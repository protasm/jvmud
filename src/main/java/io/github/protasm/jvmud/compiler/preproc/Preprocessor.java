package io.github.protasm.jvmud.compiler.preproc;

import io.github.protasm.jvmud.compiler.sourcepos.CharCursor;
import io.github.protasm.jvmud.compiler.sourcepos.LineMap;
import io.github.protasm.jvmud.compiler.sourcepos.SourceSpan;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Minimal LPC preprocessor for includes, macro definitions, conditional directives, comment
 * stripping, and line splicing with backslash-newline.
 *
 * <p>It expands macros outside of string/char literals and respects a simple hideset to avoid
 * recursive re-expansion.
 */
public final class Preprocessor {
  private static record Macro(String name, List<String> params, List<PPToken> body) {
    boolean isFunctionLike() {
      return params != null;
    }
  }

  /** Tiny token for preprocessing stage only. */
  private static record PPToken(Kind kind, String lexeme, LineMap map, int startOffset, int endOffset) {
    enum Kind {
      IDENT,
      NUMBER,
      STRING,
      OP,
      PUNCT,
      END
    }

    @Override
    public String toString() {
      return lexeme;
    }

    SourceSpan span() {
      return SourceSpan.from(map, startOffset, endOffset);
    }
  }

  private record TokenizedLine(List<PPToken> tokens, SourceSpan newlineSpan) {}

  private record FileContext(Path resolvedPath, String displayPath) {}

  private static final IncludeResolver REJECTING_INCLUDES =
      (includingFile, includePath, system) -> {
        throw new IOException("Includes are not supported without a resolver");
      };

  private final IncludeResolver resolver;
  private final Map<String, Macro> macros = new HashMap<>();
  private final Map<String, Map<String, List<PPToken>>> compatibilityFunctionPredefines =
      new HashMap<>();

  public Preprocessor(IncludeResolver resolver) {
    this(resolver, Map.of());
  }

  /**
   * Creates a preprocessor with explicit compatibility predefines.
   *
   * <p>The predefined values are replacement source text. This constructor is used by mudlib
   * boundary profiles to expose driver-compatibility macros, for example LDMud-shaped version
   * probes, without making those macros JVMud-native LPC.</p>
   *
   * @param resolver include resolver for {@code #include} directives
   * @param compatibilityPredefines macro names and replacement source text supplied by the active
   *     mudlib boundary
   */
  public Preprocessor(IncludeResolver resolver, Map<String, String> compatibilityPredefines) {
    this(resolver, compatibilityPredefines, Map.of());
  }

  /**
   * Creates a preprocessor with object-like and function-like compatibility predefines.
   *
   * <p>Function-like entries are keyed by macro name and first-argument spelling. They let mudlib
   * boundary profiles answer compile-time feature probes without hardcoding another driver's probe
   * macro names in JVMud's compiler.</p>
   *
   * @param resolver include resolver for {@code #include} directives
   * @param compatibilityPredefines object-like macro names and replacement source text supplied by
   *     the active mudlib boundary
   * @param compatibilityFunctionPredefines function-like macro replacements supplied by the active
   *     mudlib boundary
   */
  public Preprocessor(
      IncludeResolver resolver,
      Map<String, String> compatibilityPredefines,
      Map<String, Map<String, String>> compatibilityFunctionPredefines) {
    this.resolver = Objects.requireNonNull(resolver);

    // predefineds you may want:
    defineObject("__LPC__", "1");
    if (compatibilityPredefines != null) {
      compatibilityPredefines.forEach(this::defineObject);
    }
    defineCompatibilityFunctionPredefines(compatibilityFunctionPredefines);
  }

  /* ========================= public API ========================== */

  /**
   * Preprocess source text without filesystem includes.
   *
   * <p>This helper builds a {@link Preprocessor} that rejects any attempt to resolve <code>#include
   * </code> directives, making it suitable for string-only compilation flows.
   */
  public static String preprocess(String source) {
    return preprocessWithMapping(source).source();
  }

  public static PreprocessedSource preprocessWithMapping(String source) {
    Preprocessor pp = new Preprocessor(REJECTING_INCLUDES);

    return pp.preprocessWithMapping(null, source);
  }

  public static String preprocess(
      Path sourcePath, String source, String sysInclPath, String quoteInclPath) {
    IncludeResolver resolver =
        (includingFile, includePath, system) -> {
          if (!system && (includingFile != null)) {
            Path dir = includingFile.getParent();

            if (dir != null) {
              Path candidate = dir.resolve(includePath);

              if (Files.exists(candidate))
                return new IncludeResolution(
                    Files.readString(candidate), candidate.normalize(), candidate.toString());
            }
          }

          Path base = Path.of(system ? sysInclPath : quoteInclPath);

          Path resolved = base.resolve(includePath);
          return new IncludeResolution(
              Files.readString(resolved), resolved.normalize(), resolved.toString());
        };

    Preprocessor pp = new Preprocessor(resolver);

    return pp.preprocessWithMapping(sourcePath, source).source();
  }

  public static String preprocess(
      Path sourcePath, String source, Path baseIncludePath, List<Path> systemIncludePaths) {
    Preprocessor pp =
        new Preprocessor(new SearchPathIncludeResolver(baseIncludePath, systemIncludePaths));

    return pp.preprocessWithMapping(sourcePath, source).source();
  }

  public static IncludeResolver rejectingResolver() {
    return REJECTING_INCLUDES;
  }

  public String preprocess(Path sourcePath, String source) {
    return preprocessWithMapping(sourcePath, source).source();
  }

  public PreprocessedSource preprocessWithMapping(Path sourcePath, String source) {
    return preprocessWithMapping(sourcePath, source, null);
  }

  public PreprocessedSource preprocessWithMapping(
      Path sourcePath, String source, String displayPath) {
    if (source == null)
      throw new PreprocessException("Source text cannot be null.", "<unknown>", -1);

    PreprocessedSourceBuilder out = new PreprocessedSourceBuilder();
    String file =
        ((displayPath == null) || displayPath.isBlank())
            ? ((sourcePath == null) ? "<input>" : sourcePath.toString())
            : displayPath;
    LineMap map = new LineMap(file, splice(source));
    CharCursor cur = new CharCursor(map);

    try {
      expandUnit(cur, new FileContext(sourcePath, file), out, new HashSet<>());
    } catch (PreprocessException e) {
      throw e;
    } catch (RuntimeException e) {
      int line = cur.line();

      throw new PreprocessException(
          "Unexpected preprocessor failure: " + e.getMessage(), file, line, e);
    }

    return out.build(); // fully expanded source + mapping
  }

  /* ========================= core expansion ========================== */

  private void expandUnit(
      CharCursor cc, FileContext fileContext, PreprocessedSourceBuilder out, Set<String> includeGuard) {
    while (!cc.end()) {
      // buffer leading horizontal ws (not newline)
      StringBuilder bolWs = new StringBuilder();
      while (!cc.end()) {
        char p = cc.peek();

        if ((p == ' ') || (p == '\t') || (p == '\r') || (p == '\f')) {
          cc.advance();

          bolWs.append(p);
        } else break;
      }

      if (isStartOfDirective(cc)) {
        // directive: ignore buffered ws per preproc rules
        handleDirective(cc, fileContext, out, includeGuard);

        continue;
      }

      // not a directive: emit the ws we consumed and expand the rest of the line
      if (!bolWs.isEmpty()) {
        String ws = bolWs.toString();
        int startOffset = cc.index() - ws.length();
        out.append(ws, cc.map(), startOffset, cc.index());
      }

      copyLineWithExpansion(cc, out);
    }
  }

  private void handleDirective(
      CharCursor cc, FileContext fileContext, PreprocessedSourceBuilder out, Set<String> includeGuard) {
    int directiveLine = cc.line();

    cc.advance(); // consume '#'

    skipWhitespaceExceptNewline(cc);

    String name = readIdent(cc);

    if (name == null) throw error("expected directive name after '#'", cc, directiveLine);

    switch (name) {
      case "include" -> doInclude(cc, fileContext, out, includeGuard);
      case "define" -> doDefine(cc);
      case "undef" -> doUndef(cc, out);
      case "ifdef" -> doIfdef(cc, fileContext, out, includeGuard, true);
      case "ifndef" -> doIfdef(cc, fileContext, out, includeGuard, false);
      case "if" -> doIf(cc, fileContext, out, includeGuard);
      case "elif", "else", "endif" ->
          throw error("#" + name + " without matching #if", cc, directiveLine);
      default -> {
        // Unknown pragma-like directive: drop the line but preserve newline to keep
        // line numbers stable.
        skipRestOfLine(cc, out);
      }
    }
  }

  private void doInclude(
      CharCursor cc, FileContext fileContext, PreprocessedSourceBuilder out, Set<String> includeGuard) {
    skipWhitespaceExceptNewline(cc);

    char q = cc.peek();
    boolean system = false;

    if ((q == '"') || (q == '<')) {
      system = (q == '<');

      cc.advance();

      StringBuilder path = new StringBuilder();
      char endq = (q == '"') ? '"' : '>';

      while (!cc.end() && (cc.peek() != endq)) path.append(cc.advance());

      if (cc.peek() != endq) throw error("unterminated include path", cc, cc.line());

      cc.advance(); // consume closing

      skipRestOfLine(cc, out); // eat trailing until newline

      String fileText;

      IncludeResolution resolution;
      try {
        resolution = resolver.resolve(fileContext.resolvedPath(), path.toString(), system);
        fileText = resolution.source();
      } catch (IOException e) {
        throw error("cannot include '" + path + "': " + e.getMessage(), cc, cc.line());
      }

      // Preprocess included text recursively
      Path includedResolvedPath = resolution.resolvedPath();
      String includedDisplayPath =
          (resolution.displayPath() != null)
              ? resolution.displayPath()
              : ((includedResolvedPath != null)
                  ? includedResolvedPath.toString()
                  : path.toString());
      if ((includedDisplayPath == null) || includedDisplayPath.isBlank()) {
        includedDisplayPath = path.toString();
      }

      CharCursor child =
          new CharCursor(new LineMap(includedDisplayPath, splice(fileText)));

      expandUnit(
          child,
          new FileContext(includedResolvedPath, includedDisplayPath),
          out,
          includeGuard);
    } else throw error("expected \"path\" or <path> after #include", cc, cc.line());
  }

  private void doDefine(CharCursor cc) {
    skipWhitespaceExceptNewline(cc);

    String name = readIdent(cc);

    if (name == null) throw error("expected macro name after #define", cc, cc.line());

    // function-like?
    List<String> params = null;

    if (cc.peek() == '(') {
      cc.advance(); // '('

      params = new ArrayList<>();

      if (cc.peek() != ')')
        while (true) {
          skipWhitespaceExceptNewline(cc);

          String p = readIdent(cc);

          if (p == null) throw error("expected parameter name in macro", cc, cc.line());

          params.add(p);

          skipWhitespaceExceptNewline(cc);

          if (cc.peek() == ')') break;

          if (cc.peek() != ',') throw error("expected ',' or ')'", cc, cc.line());

          cc.advance();
        }

      cc.advance(); // ')'
    }

    // body = rest of line (tokenized)
    List<PPToken> body = tokenizeUntilNewline(cc).tokens();

    macros.put(name, new Macro(name, params, body));
    // keep newline (already consumed by tokenizer)
  }

  private void doUndef(CharCursor cc, PreprocessedSourceBuilder out) {
    skipWhitespaceExceptNewline(cc);

    String name = readIdent(cc);

    if (name == null) throw error("expected macro name after #undef", cc, cc.line());

    macros.remove(name);

    skipRestOfLine(cc, out); // drop to EOL
  }

  private void doIfdef(
      CharCursor cc,
      FileContext fileContext,
      PreprocessedSourceBuilder out,
      Set<String> includeGuard,
      boolean positive) {
    skipWhitespaceExceptNewline(cc);

    String name = readIdent(cc);

    if (name == null)
      throw error("expected identifier after #" + (positive ? "ifdef" : "ifndef"), cc, cc.line());

    boolean cond = macros.containsKey(name);

    if (!positive) cond = !cond;

    handleConditional(cc, fileContext, out, includeGuard, cond);
  }

  private void doIf(
      CharCursor cc, FileContext fileContext, PreprocessedSourceBuilder out, Set<String> includeGuard) {
    // Evaluate simple integer expression with defined(NAME)
    skipWhitespaceExceptNewline(cc);
    List<PPToken> expr = tokenizeUntilNewline(cc).tokens();
    boolean cond = evalIfExpr(expr);

    handleConditional(cc, fileContext, out, includeGuard, cond);
  }

  private void handleConditional(
      CharCursor cc,
      FileContext fileContext,
      PreprocessedSourceBuilder out,
      Set<String> includeGuard,
      boolean firstBranch) {
    // Consume blocks until matching #endif
    boolean taken = false;

    while (true) {
      if (!taken && firstBranch) {
        // Expand this block
          expandConditionalBlock(cc, fileContext, out, includeGuard);

        taken = true;
      } else
        // Skip this block but still handle nesting
        skipConditionalBlock(cc);

      // Expect #elif / #else / #endif
      skipWhitespaceExceptNewline(cc);
      if (!isStartOfDirective(cc)) break;

      cc.advance();

      skipWhitespaceExceptNewline(cc);

      String name = readIdent(cc);

      if ("elif".equals(name)) {
        skipWhitespaceExceptNewline(cc);
        List<PPToken> expr = tokenizeUntilNewline(cc).tokens();

        firstBranch = evalIfExpr(expr);

        continue;
      } else if ("else".equals(name)) {
        skipRestOfLine(cc, out);

        firstBranch = true; // only last branch remaining

        continue;
      } else if ("endif".equals(name)) {
        skipRestOfLine(cc, out);

        break;
      } else throw error("#" + name + " not allowed here", cc, cc.line());
    }
  }

  private void expandConditionalBlock(
      CharCursor cc, FileContext fileContext, PreprocessedSourceBuilder out, Set<String> includeGuard) {
    while (!cc.end()) {
      int mark = cc.index();
      skipWhitespaceExceptNewline(cc);

      if (isStartOfDirective(cc)) {
        // Lookahead to see if this is an #elif/#else/#endif to end this block
        cc.advance();

        skipWhitespaceExceptNewline(cc);

        String name = readIdent(cc);

        cc.rewind(mark);

        if ("elif".equals(name) || "else".equals(name) || "endif".equals(name)) {
          cc.rewind(mark);
          return; // let caller handle the directive
        }
        handleDirective(cc, fileContext, out, includeGuard);
        continue;
      }

      cc.rewind(mark);
      copyLineWithExpansion(cc, out);
    }
  }

  private static void skipWhitespaceExceptNewline(CharCursor cc) {
    while (!cc.end()) {
      char c = cc.peek();

      if ((c == ' ') || (c == '\t') || (c == '\r') || (c == '\f')) cc.advance();
      else break;
    }
  }

  private void skipConditionalBlock(CharCursor cc) {
    while (!cc.end()) {
      int mark = cc.index();
      skipWhitespaceExceptNewline(cc);

      if (isStartOfDirective(cc)) {
        cc.advance();

        skipWhitespaceExceptNewline(cc);

        String name = readIdent(cc);

        if ("if".equals(name) || "ifdef".equals(name) || "ifndef".equals(name)) {
          // nested: skip it fully
          skipRestOfLine(cc);

          skipConditionalBlock(cc);

          continue;
        }

        if ("elif".equals(name) || "else".equals(name) || "endif".equals(name)) {
          // Rewind so caller can process branch switch
          cc.rewind(mark);

          return;
        }

        // Other directive: skip its line
        cc.rewind(mark);
      } else {
        cc.rewind(mark);
      }

      // Skip one full physical line preserving newline for line numbers
      while (!cc.end()) {
        char c = cc.advance();

        if (c == '\n') break;
      }
    }
  }

  /*
   * ========================= line copying + expansion ==========================
   */

  private void copyLineWithExpansion(CharCursor cc, PreprocessedSourceBuilder out) {
    TokenizedLine toks = tokenizeUntilNewline(cc);
    List<PPToken> tokens = new ArrayList<>(toks.tokens());

    while (hasOpenFunctionMacroCall(tokens) && !cc.end()) {
      TokenizedLine next = tokenizeUntilNewline(cc);
      SourceSpan span = toks.newlineSpan();
      tokens.add(new PPToken(PPToken.Kind.PUNCT, " ", cc.map(), span.startOffset(), span.endOffset()));
      tokens.addAll(next.tokens());
      toks = next;
    }

    List<PPToken> expanded = expandMacros(tokens, new HashSet<>());

    for (PPToken t : expanded) out.append(t.lexeme(), t.map(), t.startOffset(), t.endOffset());

    if (toks.newlineSpan() != null) {
      int newlineStart = toks.newlineSpan().startOffset();
      int newlineEnd = Math.max(newlineStart + 1, toks.newlineSpan().endOffset());

      out.append("\n", cc.map(), newlineStart, newlineEnd);
    }
  }

  /*
   * ========================= tokenization (preproc level)
   * ==========================
   */

  private TokenizedLine tokenizeUntilNewline(CharCursor s) {
    List<PPToken> out = new ArrayList<>();
    SourceSpan newlineSpan = null;

    while (!s.end()) {
      int startOffset = s.index();
      char c = s.peek();

      if (c == '\n') {
        s.advance();
        newlineSpan = s.spanFrom(startOffset, s.index());

        break;
      }

      if ((c == '/') && s.canPeekNext() && (s.peekNext() == '/')) { // // comment
        int newlineStart = -1;

        while (!s.end()) {
          int at = s.index();
          char consumed = s.advance();

          if (consumed == '\n') {
            newlineStart = at;
            break;
          }
        }

        if (newlineStart >= 0)
          newlineSpan = s.spanFrom(newlineStart, s.index());

        break;
      }

      if ((c == '/') && s.canPeekNext() && (s.peekNext() == '*')) { // /* */ comment
        s.advance();
        s.advance();

        while (!s.end()) {
          if ((s.peek() == '*') && s.canPeekNext() && (s.peekNext() == '/')) {
            s.advance();
            s.advance();

            break;
          }

          s.advance();
        }

        continue;
      }

      if (Character.isWhitespace(c)) {
        s.advance();
        out.add(new PPToken(PPToken.Kind.PUNCT, String.valueOf(c), s.map(), startOffset, s.index()));

        continue;
      }

      if (c == '"') {
        out.add(readString(s, startOffset));

        continue;
      }

      if (c == '\'') {
        if (isSymbolQuote(s)) {
          s.advance();
          out.add(new PPToken(PPToken.Kind.OP, "'", s.map(), startOffset, s.index()));
        } else {
          out.add(readString(s, startOffset));
        }

        continue;
      }

      if (Character.isLetter(c) || (c == '_')) {
        out.add(readIdentTok(s, startOffset));

        continue;
      }

      if (Character.isDigit(c)) {
        out.add(readNumberTok(s, startOffset));

        continue;
      }

      // operators/punctuators (keep as single chars; good enough for macro re-expand)
      s.advance();
      out.add(new PPToken(PPToken.Kind.OP, String.valueOf(c), s.map(), startOffset, s.index()));
    }

    if (newlineSpan == null)
      newlineSpan = s.spanFrom(s.index(), s.index());

    return new TokenizedLine(out, newlineSpan);
  }

  private boolean isSymbolQuote(CharCursor s) {
    if (!s.canPeekNext())
      return false;

    char next = s.peekNext();
    if (!(Character.isLetter(next) || (next == '_')))
      return false;

    char afterIdentifierStart = s.map().charAt(s.index() + 2);
    return afterIdentifierStart != '\'';
  }

  private PPToken readIdentTok(CharCursor s, int startOffset) {
    StringBuilder sb = new StringBuilder();

    while (!s.end()) {
      char ch = s.peek();

      if (Character.isLetterOrDigit(ch) || (ch == '_')) sb.append(s.advance());
      else break;
    }

    return new PPToken(PPToken.Kind.IDENT, sb.toString(), s.map(), startOffset, s.index());
  }

  private PPToken readNumberTok(CharCursor s, int startOffset) {
    StringBuilder sb = new StringBuilder();

    while (!s.end() && Character.isDigit(s.peek())) sb.append(s.advance());

    if (!s.end() && (s.peek() == '.')) {
      sb.append(s.advance());

      while (!s.end() && Character.isDigit(s.peek())) sb.append(s.advance());
    }

    return new PPToken(PPToken.Kind.NUMBER, sb.toString(), s.map(), startOffset, s.index());
  }

  private PPToken readString(CharCursor s, int startOffset) {
    StringBuilder sb = new StringBuilder();
    char q = s.advance(); // opening

    sb.append(q);

    while (!s.end()) {
      char c = s.advance();

      sb.append(c);

      if (c == q) break;

      if ((c == '\\') && !s.end()) sb.append(s.advance());
    }
    return new PPToken(PPToken.Kind.STRING, sb.toString(), s.map(), startOffset, s.index());
  }

  private void skipRestOfLine(CharCursor s, PreprocessedSourceBuilder out) {
    int newlineStart = -1;

    while (!s.end()) {
      char c = s.advance();

      if (c == '\n') {
        newlineStart = s.index() - 1;
        break;
      }
    }

    int startOffset = (newlineStart >= 0) ? newlineStart : s.index();
    out.append("\n", s.map(), startOffset, startOffset + 1); // preserve line count
  }

  private void skipRestOfLine(CharCursor s) {
    while (!s.end() && (s.advance() != '\n')) {
      // just advance
    }
  }

  /* ========================= macros ========================== */

  private void defineObject(String name, String body) {
    macros.put(name, new Macro(name, null, List.of(syntheticToken(body))));
  }

  private void defineCompatibilityFunctionPredefines(Map<String, Map<String, String>> configured) {
    if (configured == null || configured.isEmpty()) return;

    configured.forEach(
        (macroName, replacements) -> {
          if (macroName == null || macroName.isBlank() || replacements == null || replacements.isEmpty())
            return;

          Map<String, List<PPToken>> tokenized = new HashMap<>();
          replacements.forEach(
              (argument, replacement) -> {
                if (argument != null && !argument.isBlank() && replacement != null && !replacement.isBlank())
                  tokenized.put(argument.trim(), tokenizeReplacement(replacement));
              });
          if (!tokenized.isEmpty()) compatibilityFunctionPredefines.put(macroName.trim(), tokenized);
        });
  }

  private List<PPToken> tokenizeReplacement(String replacement) {
    return tokenizeUntilNewline(new CharCursor(new LineMap("<built-in>", replacement))).tokens();
  }

  private PPToken syntheticToken(String body) {
    LineMap map = new LineMap("<built-in>", body);

    return new PPToken(PPToken.Kind.IDENT, body, map, 0, body.length());
  }

  private List<PPToken> expandMacros(List<PPToken> in, Set<String> hideset) {
    List<PPToken> out = new ArrayList<>();

    for (int i = 0; i < in.size(); ) {
      PPToken t = in.get(i);

      if (i > 0 && "'".equals(in.get(i - 1).lexeme)) {
        out.add(t);
        i++;
        continue;
      }

      if ((t.kind == PPToken.Kind.IDENT)
          && compatibilityFunctionPredefines.containsKey(t.lexeme)
          && !hideset.contains(t.lexeme)) {
        ParsedMacroCall call = parseMacroCall(in, i);
        if (call == null) {
          out.add(t);
          i++;
          continue;
        }

        List<PPToken> replacement = compatibilityReplacement(t.lexeme, call.actuals());
        if (replacement == null) {
          out.add(t);
          i++;
          continue;
        }

        Set<String> nextHide = new HashSet<>(hideset);
        nextHide.add(t.lexeme);
        out.addAll(expandMacros(replacement, nextHide));
        i = call.nextIndex();
      } else if ((t.kind == PPToken.Kind.IDENT)
          && macros.containsKey(t.lexeme)
          && !hideset.contains(t.lexeme)) {
        Macro m = macros.get(t.lexeme);

        if (!m.isFunctionLike()) {
          // object-like: splice body, with hideset entry
          Set<String> nextHide = new HashSet<>(hideset);

          nextHide.add(m.name());

          out.addAll(expandMacros(m.body, nextHide));

          i++;
        } else {
          // function-like: collect actual args
          ParsedMacroCall call = parseMacroCall(in, i);
          if (call == null) {
            // not a call site; leave as-is
            out.add(t);
            i++;

            continue;
          }

          // substitute params
          Map<String, List<PPToken>> paramMap = new HashMap<>();

          if (m.params != null)
            for (int pi = 0; pi < m.params.size(); pi++) {
              List<PPToken> val = (pi < call.actuals().size()) ? call.actuals().get(pi) : List.of();

              paramMap.put(m.params.get(pi), val);
            }

          List<PPToken> substituted = substitute(m.body, paramMap);
          Set<String> nextHide = new HashSet<>(hideset);

          nextHide.add(m.name());

          out.addAll(expandMacros(substituted, nextHide));
          i = call.nextIndex();
        }
      } else {
        out.add(t);

        i++;
      }
    }

    return out;
  }

  private record ParsedMacroCall(List<List<PPToken>> actuals, int nextIndex) {}

  private ParsedMacroCall parseMacroCall(List<PPToken> in, int macroIndex) {
    int i = macroIndex + 1;
    if ((i >= in.size()) || !in.get(i).lexeme.equals("(")) return null;

    i++;
    List<List<PPToken>> actuals = new ArrayList<>();
    List<PPToken> current = new ArrayList<>();
    int depth = 1;

    while ((i < in.size()) && (depth > 0)) {
      PPToken x = in.get(i++);

      if (x.lexeme.equals("(")) {
        depth++;
        current.add(x);
        continue;
      }

      if (x.lexeme.equals(")")) {
        depth--;
        if (depth == 0) {
          actuals.add(current);
          return new ParsedMacroCall(actuals, i);
        }
        current.add(x);
        continue;
      }

      if (x.lexeme.equals(",") && (depth == 1)) {
        actuals.add(current);
        current = new ArrayList<>();
      } else current.add(x);
    }

    return null;
  }

  private List<PPToken> compatibilityReplacement(String macroName, List<List<PPToken>> actuals) {
    if (actuals.isEmpty()) return null;

    String argumentKey = argumentKey(actuals.get(0));
    if (argumentKey.isBlank()) return null;

    Map<String, List<PPToken>> replacements = compatibilityFunctionPredefines.get(macroName);
    return replacements != null ? replacements.get(argumentKey) : null;
  }

  private String argumentKey(List<PPToken> argument) {
    StringBuilder key = new StringBuilder();
    for (PPToken token : argument) key.append(token.lexeme);
    return key.toString().trim();
  }

  private List<PPToken> substitute(List<PPToken> body, Map<String, List<PPToken>> params) {
    if (params.isEmpty()) return body;

    List<PPToken> out = new ArrayList<>();

    for (PPToken t : body)
      if ((t.kind == PPToken.Kind.IDENT) && params.containsKey(t.lexeme))
        out.addAll(params.get(t.lexeme));
      else out.add(t);
    return out;
  }

  private boolean hasOpenFunctionMacroCall(List<PPToken> tokens) {
    for (int i = 0; i < tokens.size(); i++) {
      PPToken token = tokens.get(i);

      if (token.kind != PPToken.Kind.IDENT)
        continue;

      Macro macro = macros.get(token.lexeme);
      if (macro == null || !macro.isFunctionLike())
        continue;

      int j = i + 1;
      while (j < tokens.size() && tokens.get(j).lexeme.isBlank())
        j++;

      if (j >= tokens.size() || !"(".equals(tokens.get(j).lexeme))
        continue;

      int depth = 0;
      for (; j < tokens.size(); j++) {
        String lexeme = tokens.get(j).lexeme;
        if ("(".equals(lexeme))
          depth++;
        else if (")".equals(lexeme))
          depth--;
      }

      if (depth > 0)
        return true;
    }

    return false;
  }

  /* ========================= #if expression ========================== */

  private boolean evalIfExpr(List<PPToken> rawExpression) {
    List<PPToken> expr = expandIfExpression(rawExpression);

    // Recursive-descent over ||, &&, !, parentheses, NUMBER, defined(IDENT)
    class P {
      int i = 0;

      PPToken la() {
        return i < expr.size()
            ? expr.get(i)
            : new PPToken(PPToken.Kind.END, "", new LineMap("<expr>", ""), 0, 0);
      }

      PPToken eat() {
        return expr.get(i++);
      }

      boolean parseOr() {
        boolean v = parseAnd();

        while ((i < expr.size()) && "||".equals(expr.get(i).lexeme)) {
          i++;

          v = v || parseAnd();
        }

        return v;
      }

      boolean parseAnd() {
        boolean v = parseUnary();

        while ((i < expr.size()) && "&&".equals(expr.get(i).lexeme)) {
          i++;

          v = v && parseUnary();
        }

        return v;
      }

      boolean parseUnary() {
        if ((i < expr.size()) && "!".equals(expr.get(i).lexeme)) {
          i++;

          return !parseUnary();
        }

        if ((i < expr.size()) && "(".equals(expr.get(i).lexeme)) {
          i++;

          boolean v = parseOr();

          if ((i < expr.size()) && ")".equals(expr.get(i).lexeme)) i++;

          return v;
        }

        if ((i < expr.size())
            && (expr.get(i).kind == PPToken.Kind.IDENT)
            && "defined".equals(expr.get(i).lexeme)) {
          i++;

          boolean paren = (i < expr.size()) && "(".equals(expr.get(i).lexeme);

          if (paren) i++;

          String id =
              ((i < expr.size()) && (expr.get(i).kind == PPToken.Kind.IDENT)) ? eat().lexeme : "";

          if (paren && (i < expr.size()) && ")".equals(expr.get(i).lexeme)) i++;

          return macros.containsKey(id);
        }
        // numbers: non-zero => true
        if ((i < expr.size()) && (expr.get(i).kind == PPToken.Kind.NUMBER))
          try {
            String n = eat().lexeme;
            double d = Double.parseDouble(n);

            return d != 0.0;
          } catch (NumberFormatException e) {
            return false;
          }
        // unknown identifiers treated as 0
        if ((i < expr.size()) && (expr.get(i).kind == PPToken.Kind.IDENT)) {
          i++;

          return false;
        }

        return false;
      }
    }

    return new P().parseOr();
  }

  /** Expands conditional-expression macros while preserving operands of {@code defined}. */
  private List<PPToken> expandIfExpression(List<PPToken> expression) {
    List<PPToken> expanded = new ArrayList<>();
    List<PPToken> ordinary = new ArrayList<>();

    for (int i = 0; i < expression.size(); i++) {
      PPToken token = expression.get(i);
      if (token.kind != PPToken.Kind.IDENT || !"defined".equals(token.lexeme)) {
        ordinary.add(token);
        continue;
      }

      expanded.addAll(expandMacros(ordinary, Set.of()));
      ordinary.clear();
      expanded.add(token);

      if (++i >= expression.size()) break;
      PPToken operand = expression.get(i);
      expanded.add(operand);
      if (!"(".equals(operand.lexeme)) continue;

      if (++i < expression.size()) expanded.add(expression.get(i));
      if (++i < expression.size()) expanded.add(expression.get(i));
    }

    expanded.addAll(expandMacros(ordinary, Set.of()));
    expanded.removeIf(token -> token.lexeme.isBlank());
    return expanded;
  }

  /* ========================= utils ========================== */

  private PreprocessException error(String msg, CharCursor cc, int atLine) {
    return new PreprocessException(msg, cc.fileName(), atLine);
  }

  private String readIdent(CharCursor s) {
    StringBuilder sb = new StringBuilder();
    char c = s.peek();

    if (!(Character.isLetter(c) || (c == '_'))) return null;

    sb.append(s.advance());

    while (!s.end()) {
      char ch = s.peek();

      if (Character.isLetterOrDigit(ch) || (ch == '_')) sb.append(s.advance());
      else break;
    }

    return sb.toString();
  }

  private static boolean isStartOfDirective(CharCursor cc) {
    if (cc.end() || (cc.peek() != '#'))
      return false;

    int mark = cc.index();
    cc.advance();
    skipWhitespaceExceptNewline(cc);
    boolean directive = !cc.end() && (Character.isLetter(cc.peek()) || (cc.peek() == '_'));
    cc.rewind(mark);
    return directive;
  }

  /** Handle backslash-newline line splicing up-front. */
  private static String splice(String text) {
    StringBuilder out = new StringBuilder(text.length());

    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);

      if ((c == '\\') && ((i + 1) < text.length()) && (text.charAt(i + 1) == '\n')) {
        i++; // drop both

        continue;
      }

      out.append(c);
    }

    return out.toString();
  }

}
