package io.github.protasm.jvmud.compiler.parser;

import static io.github.protasm.jvmud.compiler.token.TokenType.T_BREAK;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_AMP_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_CASE;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_CARET_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_COLON;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_COMMA;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_CONTINUE;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_DBL_AMP_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_DBL_PIPE_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_DEFAULT;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_DO;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_ELSE;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_FOREACH;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_IDENTIFIER;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_IF;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_IN;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_INHERIT;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_FOR;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LEFT_BRACE;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LEFT_BRACKET;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LEFT_PAREN;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LESS_LESS_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_MINUS_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_PIPE_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_PLUS_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_GREATER_GREATER_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_RETURN;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_RIGHT_BRACE;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_RIGHT_BRACKET;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_RIGHT_PAREN;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_SEMICOLON;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_SLASH_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_STAR_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_STRING_LITERAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_SWITCH;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_WHILE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.protasm.jvmud.compiler.parser.ast.ASTArgument;
import io.github.protasm.jvmud.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.compiler.parser.ast.ASTField;
import io.github.protasm.jvmud.compiler.parser.ast.ASTInherit;
import io.github.protasm.jvmud.compiler.parser.ast.ASTLocal;
import io.github.protasm.jvmud.compiler.parser.ast.ASTMethod;
import io.github.protasm.jvmud.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.compiler.parser.ast.ASTParameter;
import io.github.protasm.jvmud.compiler.parser.ast.ASTParameters;
import io.github.protasm.jvmud.compiler.parser.ast.DeclarationModifiers;
import io.github.protasm.jvmud.compiler.parser.ast.DeclarationModifiers.Visibility;
import io.github.protasm.jvmud.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLocalStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprProtectedEval;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprSequence;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTStatement;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtBreak;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtContinue;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtDoWhile;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtFor;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtForeach;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtSwitch;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtWhile;
import io.github.protasm.jvmud.compiler.parser.parselet.InfixParselet;
import io.github.protasm.jvmud.compiler.parser.parselet.PrefixParselet;
import io.github.protasm.jvmud.compiler.preproc.Preprocessor;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import io.github.protasm.jvmud.compiler.token.Token;
import io.github.protasm.jvmud.compiler.token.TokenClassifier;
import io.github.protasm.jvmud.compiler.token.TokenType;
import io.github.protasm.jvmud.compiler.token.TokenList;

public class Parser {
        private TokenList tokens;
        private ASTObject currObj;
        private Locals locals;
        private ASTMethod currentMethod;
        private final ParserOptions options;
        private final RuntimeContext runtimeContext;
        private final Map<String, Integer> fieldDefinitionIndex = new HashMap<>();
        private final Map<String, Integer> methodDefinitionIndex = new HashMap<>();
        private int sourceOrder;

        public Parser() {
                this(new RuntimeContext(Preprocessor.rejectingResolver()), ParserOptions.defaults());
        }

        public Parser(ParserOptions options) {
                this(new RuntimeContext(Preprocessor.rejectingResolver()), options);
        }

        public Parser(RuntimeContext runtimeContext) {
                this(runtimeContext, ParserOptions.defaults());
        }

        public Parser(RuntimeContext runtimeContext, ParserOptions options) {
                this.runtimeContext = (runtimeContext != null) ? runtimeContext : new RuntimeContext(Preprocessor.rejectingResolver());
                this.options = (options == null) ? ParserOptions.defaults() : options;
        }

    public TokenList tokens() {
        return this.tokens;
    }

    public ASTObject currObj() {
        return this.currObj;
    }

    public Locals locals() {
        return this.locals;
    }

    public RuntimeContext runtimeContext() {
        return runtimeContext;
    }

    public int currLine() {
        return tokens.current().line();
    }

    public ASTObject parse(String objName, TokenList tokens) {
                if (tokens == null)
                        throw new ParseException("Token list cannot be null.");

                this.tokens = TokenClassifier.classify(tokens);

                try {
                        currObj = new ASTObject(0, objName);
                        fieldDefinitionIndex.clear();
                        methodDefinitionIndex.clear();
                        sourceOrder = 0;

                        while (!this.tokens.isAtEnd()) {
                                ASTInherit inherit = inheritDeclaration();
                                if (inherit != null) {
                                        inherit.setSourceOrder(nextSourceOrder());
                                        currObj.addInherit(inherit);
                                        continue;
                                }

                                property();
                        }

                        return currObj;
                } catch (ParseException e) {
                        throw e;
                } catch (RuntimeException e) {
                        Token<?> current = (this.tokens != null) ? this.tokens.current() : null;

                        if (current != null)
                                throw new ParseException("Unexpected parser failure: " + e.getMessage(), current, e);

                        throw new ParseException("Unexpected parser failure: " + e.getMessage(), -1, e);
                }
    }

    private ASTInherit inheritDeclaration() {
            boolean isVirtual = false;

            if (tokens.check(T_IDENTIFIER) && "virtual".equals(tokens.current().lexeme())
                    && tokens.peek(1).type() == T_INHERIT) {
                    isVirtual = true;
                    tokens.advance();
            }

            if (!tokens.match(T_INHERIT)) {
                    return null;
            }

            Token<String> parentToken = consumeInheritPath();
            return new ASTInherit(parentToken.line(), parentToken.lexeme(), isVirtual);
    }

    private Token<String> consumeInheritPath() {
        Token<String> parentToken = tokens.consume(T_STRING_LITERAL, "Expect string after 'inherit'.");

        tokens.consume(T_SEMICOLON, "Expect ';' after inherited object path.");

        return parentToken;
    }

    private void property() {
        DeclarationModifiers modifiers = declarationModifiers();
        Symbol symbol = declarationSymbol();
        int declarationLine = tokens.previous().line();
        boolean hasType = symbol.declaredTypeName() != null;

        if (tokens.match(T_LEFT_PAREN))
            method(symbol, declarationLine, modifiers);
        else if (hasType)
            field(symbol, declarationLine, modifiers);
        else
            throw new ParseException("Untyped declarations must be functions.", tokens.current());
    }

    private DeclarationModifiers declarationModifiers() {
        Visibility visibility = Visibility.DEFAULT;
        boolean isStatic = false;
        boolean isNomask = false;
        boolean isVarargs = false;
        boolean isNosave = false;
        boolean isDeprecated = false;

        while (tokens.check(T_IDENTIFIER)) {
            String lexeme = tokens.current().lexeme();

            switch (lexeme) {
            case "public" -> {
                if (visibility == Visibility.PUBLIC) {
                    tokens.advance();
                    continue;
                }
                if (visibility != Visibility.DEFAULT)
                    throw new ParseException("Only one visibility modifier is allowed.", tokens.current());
                visibility = Visibility.PUBLIC;
                tokens.advance();
            }
            case "private" -> {
                if (visibility == Visibility.PRIVATE) {
                    tokens.advance();
                    continue;
                }
                if (visibility != Visibility.DEFAULT)
                    throw new ParseException("Only one visibility modifier is allowed.", tokens.current());
                visibility = Visibility.PRIVATE;
                tokens.advance();
            }
            case "protected" -> {
                if (visibility == Visibility.PROTECTED) {
                    tokens.advance();
                    continue;
                }
                if (visibility != Visibility.DEFAULT)
                    throw new ParseException("Only one visibility modifier is allowed.", tokens.current());
                visibility = Visibility.PROTECTED;
                tokens.advance();
            }
            case "static" -> {
                isStatic = true;
                tokens.advance();
            }
            case "nomask" -> {
                isNomask = true;
                tokens.advance();
            }
            case "varargs" -> {
                isVarargs = true;
                tokens.advance();
            }
            case "nosave" -> {
                isNosave = true;
                tokens.advance();
            }
            case "deprecated" -> {
                isDeprecated = true;
                tokens.advance();
            }
            default -> {
                return new DeclarationModifiers(visibility, isStatic, isNomask, isVarargs, isNosave, isDeprecated);
            }
            }
        }

        return new DeclarationModifiers(visibility, isStatic, isNomask, isVarargs, isNosave, isDeprecated);
    }

    private Symbol declarationSymbol() {
        if (!tokens.check(T_IDENTIFIER))
            throw new ParseException("Expect property type or name.", tokens.current());

        Token<String> firstToken = tokens.consume(T_IDENTIFIER, "Expect property type or name.");
        boolean isArrayType = tokens.match(TokenType.T_STAR);

        if (tokens.check(T_IDENTIFIER)) {
            Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect property name.");
            String declaredTypeName = firstToken.lexeme() + (isArrayType ? "*" : "");
            return new Symbol(declaredTypeName, nameToken.lexeme());
        }

        return new Symbol((String) null, firstToken.lexeme());
    }

    private void field(Symbol symbol, int declarationLine, DeclarationModifiers modifiers) {
        if (modifiers.isVarargs())
            throw new ParseException("The 'varargs' modifier is only valid on methods.", tokens.current());

        if (locals == null)
            locals = new Locals();

        List<FieldDeclarator> declarators = fieldDeclarators(symbol);

        for (FieldDeclarator declarator : declarators) {
            boolean hasInitializer = declarator.initializer() != null;
            if (!hasInitializer) {
                ASTField field = new ASTField(
                        declarationLine,
                        currObj.name(),
                        declarator.symbol(),
                        true,
                        modifiers);
                field.setSourceOrder(nextSourceOrder());
                currObj.fields().put(field.symbol().name(), field);
                continue;
            }

            int definitionIndex = nextFieldDefinitionIndex(declarator.symbol().name());
            ASTField field = currObj.fields().get(declarator.symbol().name(), definitionIndex);

            if (field == null) {
                field = new ASTField(
                        declarationLine,
                        currObj.name(),
                        declarator.symbol(),
                        true,
                        modifiers);
                field.setSourceOrder(nextSourceOrder());
                currObj.fields().put(field.symbol().name(), field);
            }

            field.markDefined();
            field.setInitializer(declarator.initializer());
        }
    }

    private int nextFieldDefinitionIndex(String name) {
        return fieldDefinitionIndex.merge(name, 1, Integer::sum) - 1;
    }

    private List<FieldDeclarator> fieldDeclarators(Symbol symbol) {
        List<FieldDeclarator> declarators = new ArrayList<>();

        declarators.add(fieldDeclarator(symbol));

        while (tokens.match(T_COMMA)) {
            boolean isArrayType = tokens.match(TokenType.T_STAR);
            Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect field name.");
            String declaredTypeName = symbol.declaredTypeName();
            if (isArrayType && declaredTypeName != null && !declaredTypeName.endsWith("*"))
                declaredTypeName = declaredTypeName + "*";
            Symbol additionalSymbol = new Symbol(declaredTypeName, nameToken.lexeme());

            declarators.add(fieldDeclarator(additionalSymbol));
        }

        tokens.consume(T_SEMICOLON, "Expect ';' after field declaration.");

        return declarators;
    }

    private FieldDeclarator fieldDeclarator(Symbol symbol) {
        ASTExpression initializer = null;

        if (tokens.match(T_EQUAL))
            initializer = expression();

        return new FieldDeclarator(symbol, initializer);
    }

        private void method(Symbol symbol, int declarationLine, DeclarationModifiers modifiers) {
                locals = new Locals();
                ParsedParameters parsedParams = parameters();
                ASTParameters params = parsedParams.parameters();

                if (tokens.match(T_SEMICOLON)) {
                        ASTMethod declaration = new ASTMethod(
                                declarationLine,
                                currObj.name(),
                                symbol,
                                true,
                                modifiers);
                        declaration.setSourceOrder(nextSourceOrder());
                        declaration.setParameters(params);
                        parsedParams.locals().forEach(declaration::addLocal);
                        currObj.methods().put(declaration.symbol().name(), declaration);
                        currentMethod = null;
                        return;
                }

                int definitionIndex = nextMethodDefinitionIndex(symbol.name());
                ASTMethod method = currObj.methods().get(symbol.name(), definitionIndex);

                if (method == null) {
                        method = new ASTMethod(
                                declarationLine,
                                currObj.name(),
                                symbol,
                                true,
                                modifiers);
                        method.setSourceOrder(nextSourceOrder());
                        currObj.methods().put(method.symbol().name(), method);
                }

                method.setParameters(params);
                method.clearLocals();
                parsedParams.locals().forEach(method::addLocal);

                currentMethod = method;

                method.markDefined();

                tokens.consume(T_LEFT_BRACE, "Expect '{' after method declaration.");

                method.setBody(block(true));

                currentMethod = null;
        }

    private int nextMethodDefinitionIndex(String name) {
        return methodDefinitionIndex.merge(name, 1, Integer::sum) - 1;
    }

    private int nextSourceOrder() {
        return sourceOrder++;
    }

    private ParsedParameters parameters() {
        ASTParameters params = new ASTParameters(currLine());
        List<ASTLocal> paramLocals = new ArrayList<>();

        if (tokens.match(T_RIGHT_PAREN)) // No parameters
            return new ParsedParameters(params, paramLocals);

        do {
            Token<String> firstToken = tokens.consume(T_IDENTIFIER, "Expect parameter name or type.");
            Token<String> nameToken = null;
            String declaredType = null;

            boolean isArrayType = tokens.match(TokenType.T_STAR);

            if (tokens.check(T_IDENTIFIER)) {
                nameToken = tokens.consume(T_IDENTIFIER, "Expect parameter name.");
                declaredType = firstToken.lexeme() + (isArrayType ? "*" : "");
            } else {
                nameToken = firstToken;
            }

            Symbol symbol = new Symbol(declaredType, nameToken.lexeme());

                        ASTParameter param = new ASTParameter(currLine(), symbol);
                        ASTLocal local = new ASTLocal(currLine(), symbol);

                        params.add(param);

            locals.add(local);
            paramLocals.add(local);

        } while (tokens.match(T_COMMA));

        tokens.consume(T_RIGHT_PAREN, "Expect ')' after method parameters.");

        return new ParsedParameters(params, paramLocals);
    }

    public ASTArguments arguments() {
        ASTArguments args = new ASTArguments(currLine());

        tokens.consume(T_LEFT_PAREN, "Expect '(' after method name.");

        if (tokens.match(T_RIGHT_PAREN)) // No arguments
            return args;

        do {
            ASTExpression expr = expression();
            ASTArgument arg = new ASTArgument(currLine(), expr);

            args.add(arg);
        } while (tokens.match(T_COMMA));

        tokens.consume(T_RIGHT_PAREN, "Expect ')' after method arguments.");

        return args;
    }

    public ASTExpression protectedEval(int line) {
        tokens.consume(T_LEFT_PAREN, "Expect '(' after protected evaluation.");

        List<ASTExpression> expressions = new ArrayList<>();
        boolean suppressLogging = false;

        while (!tokens.check(T_RIGHT_PAREN) && !tokens.isAtEnd()) {
            if (tokens.check(T_IDENTIFIER) && "nolog".equals(tokens.current().lexeme())
                    && tokens.peek(1).type() == T_RIGHT_PAREN) {
                suppressLogging = true;
                tokens.advance();
                break;
            }

            expressions.add(expression());

            if (!tokens.match(T_SEMICOLON))
                break;
        }

        tokens.consume(T_RIGHT_PAREN, "Expect ')' after protected evaluation.");

        if (expressions.isEmpty())
            throw new ParseException("Protected evaluation requires an expression.", tokens.previous());

        ASTExpression body = expressions.size() == 1
                ? expressions.get(0)
                : new ASTExprSequence(line, expressions);
        return new ASTExprProtectedEval(line, body, suppressLogging);
    }

        private ASTStmtBlock block(boolean isMethodBody) {
                locals.beginScope();

                List<ASTStatement> statements = new ArrayList<>();
                List<ASTStmtBlock.BlockLocalDeclaration> localDeclarations = new ArrayList<>();

                while (!tokens.check(T_RIGHT_BRACE) && !tokens.isAtEnd())
                        if (startsLocalDeclaration()) { // local declaration
                                Token<String> typeToken = tokens.consume(T_IDENTIFIER, "Expect local type.");

                                List<ASTLocal> declaredLocals = locals(typeToken, statements);
                                if (!declaredLocals.isEmpty())
                                        localDeclarations.add(
                                                        new ASTStmtBlock.BlockLocalDeclaration(statements.size(), declaredLocals));
                        } else
                                statements.add(statement());

                tokens.consume(T_RIGHT_BRACE, isMethodBody ? "Expect '}' after method body."
                                : "Expect '}' after block.");

                locals.endScope();

                return new ASTStmtBlock(currLine(), statements, localDeclarations);
        }

    private List<ASTLocal> locals(Token<String> typeToken, List<ASTStatement> statements) {
        List<ASTLocal> declaredLocals = new ArrayList<>();

        do {
            boolean isArrayType = tokens.match(TokenType.T_STAR);
            Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect local variable name.");
            String declaredType = typeToken.lexeme() + (isArrayType ? "*" : "");
            Symbol symbol = new Symbol(declaredType, nameToken.lexeme());

            ASTLocal local = new ASTLocal(currLine(), symbol);

            locals.add(local);
            declaredLocals.add(local);
            if (currentMethod != null)
                currentMethod.addLocal(local);

            if (tokens.match(T_EQUAL)) { // local assignment
                ASTExprLocalStore expr = new ASTExprLocalStore(currLine(), local, expression(), true);
                ASTStmtExpression exprStmt = new ASTStmtExpression(currLine(), expr);

                statements.add(exprStmt);
            }
        } while (tokens.match(T_COMMA));

        tokens.consume(T_SEMICOLON, "Expect ';' after local variable declaration.");

        return declaredLocals;
    }

        public ASTStatement statement() {
                if (tokens.match(T_IF))
                        return ifStatement();
                else if (tokens.match(T_DO))
                        return doWhileStatement();
                else if (tokens.match(T_FOR))
                        return forStatement();
                else if (tokens.match(T_FOREACH))
                        return foreachStatement();
                else if (tokens.match(T_WHILE))
                        return whileStatement();
                else if (tokens.match(T_SWITCH))
                        return switchStatement();
                else if (tokens.match(T_BREAK))
                        return breakStatement();
                else if (tokens.match(T_CONTINUE))
                        return continueStatement();
                else if (tokens.match(T_RETURN))
                        return returnStatement();
                else if (tokens.match(T_LEFT_BRACE))
                        return block(false);
        else
                        return expressionStatement();
        }

    private record ParsedParameters(ASTParameters parameters, List<ASTLocal> locals) {}

    private static class FieldDeclarator {
        private final Symbol symbol;
        private final ASTExpression initializer;

        FieldDeclarator(Symbol symbol, ASTExpression initializer) {
            this.symbol = symbol;
            this.initializer = initializer;
        }

        Symbol symbol() {
            return symbol;
        }

        ASTExpression initializer() {
            return initializer;
        }
    }

    private ASTStatement ifStatement() {
        ASTExpression expr = ifCondition();
        ASTStatement stmtThen = statement();

        if (tokens.match(T_ELSE))
            return new ASTStmtIfThenElse(currLine(), expr, stmtThen, statement());
        else
            return new ASTStmtIfThenElse(currLine(), expr, stmtThen, null);
    }

    private ASTExpression ifCondition() {
        tokens.consume(T_LEFT_PAREN, "Expect '(' after if.");

        ASTExpression expr = expression();

        tokens.consume(T_RIGHT_PAREN, "Expect ')' after if condition.");

        return expr;
    }

    private ASTStmtReturn returnStatement() {
        if (tokens.match(T_SEMICOLON)) {
            return new ASTStmtReturn(currLine(), null);
        }

        ASTExpression expr = expression();

        tokens.consume(T_SEMICOLON, "Expect ';' after return statement.");

        return new ASTStmtReturn(currLine(), expr);
    }

    private ASTStatement breakStatement() {
        tokens.consume(T_SEMICOLON, "Expect ';' after break.");
        return new ASTStmtBreak(currLine());
    }

    private ASTStatement continueStatement() {
        tokens.consume(T_SEMICOLON, "Expect ';' after continue.");
        return new ASTStmtContinue(currLine());
    }

    private ASTStmtSwitch switchStatement() {
        int line = tokens.previous().line();
        tokens.consume(T_LEFT_PAREN, "Expect '(' after switch.");
        ASTExpression expression = expression();
        tokens.consume(T_RIGHT_PAREN, "Expect ')' after switch expression.");
        tokens.consume(T_LEFT_BRACE, "Expect '{' after switch expression.");

        List<ASTStmtSwitch.SwitchCase> cases = new ArrayList<>();
        while (!tokens.check(T_RIGHT_BRACE) && !tokens.isAtEnd()) {
            int caseLine = currLine();
            ASTExpression caseExpression = null;
            boolean isDefault = false;

            if (tokens.match(T_CASE)) {
                caseLine = tokens.previous().line();
                caseExpression = expression();
                tokens.consume(T_COLON, "Expect ':' after case expression.");
            } else if (tokens.match(T_DEFAULT)) {
                caseLine = tokens.previous().line();
                isDefault = true;
                tokens.consume(T_COLON, "Expect ':' after default.");
            } else {
                throw new ParseException("Expect 'case' or 'default' in switch body.", tokens.current());
            }

            List<ASTStatement> statements = new ArrayList<>();
            while (!tokens.check(T_CASE)
                    && !tokens.check(T_DEFAULT)
                    && !tokens.check(T_RIGHT_BRACE)
                    && !tokens.isAtEnd()) {
                if (startsLocalDeclaration()) {
                    Token<String> typeToken = tokens.consume(T_IDENTIFIER, "Expect local type.");
                    locals(typeToken, statements);
                } else {
                    statements.add(statement());
                }
            }

            cases.add(new ASTStmtSwitch.SwitchCase(caseLine, caseExpression, isDefault, statements));
        }

        tokens.consume(T_RIGHT_BRACE, "Expect '}' after switch body.");
        return new ASTStmtSwitch(line, expression, cases);
    }

    private ASTStmtWhile whileStatement() {
        int line = tokens.previous().line();
        tokens.consume(T_LEFT_PAREN, "Expect '(' after while.");

        ASTExpression condition = expression();

        tokens.consume(T_RIGHT_PAREN, "Expect ')' after while condition.");

        return new ASTStmtWhile(line, condition, statement());
    }

    private ASTStmtDoWhile doWhileStatement() {
        int line = tokens.previous().line();
        ASTStatement body = statement();

        tokens.consume(T_WHILE, "Expect 'while' after do body.");
        tokens.consume(T_LEFT_PAREN, "Expect '(' after do/while.");

        ASTExpression condition = expression();

        tokens.consume(T_RIGHT_PAREN, "Expect ')' after do/while condition.");
        tokens.consume(T_SEMICOLON, "Expect ';' after do/while condition.");

        return new ASTStmtDoWhile(line, body, condition);
    }

    private ASTStmtFor forStatement() {
        int line = tokens.previous().line();
        tokens.consume(T_LEFT_PAREN, "Expect '(' after for.");

        List<ASTLocal> initializerLocals = List.of();
        ASTExpression initializer = null;
        if (!tokens.check(T_SEMICOLON)) {
            if (startsLocalDeclaration()) {
                ForInitializerDeclaration declaration = forInitializerDeclaration();
                initializerLocals = declaration.locals();
                initializer = declaration.initializer();
            } else {
                initializer = commaExpression();
            }
        }
        tokens.consume(T_SEMICOLON, "Expect ';' after for initializer.");

        ASTExpression condition = null;
        if (!tokens.check(T_SEMICOLON))
            condition = expression();
        tokens.consume(T_SEMICOLON, "Expect ';' after for condition.");

        ASTExpression update = null;
        if (!tokens.check(T_RIGHT_PAREN))
            update = commaExpression();
        tokens.consume(T_RIGHT_PAREN, "Expect ')' after for clauses.");

        ASTStatement body = statement();

        return new ASTStmtFor(line, initializerLocals, initializer, condition, update, body);
    }

    private record ForInitializerDeclaration(List<ASTLocal> locals, ASTExpression initializer) {}

    private ForInitializerDeclaration forInitializerDeclaration() {
        Token<String> typeToken = tokens.consume(T_IDENTIFIER, "Expect for initializer local type.");
        boolean isArrayType = tokens.match(TokenType.T_STAR);
        Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect for initializer local name.");
        String declaredType = typeToken.lexeme() + (isArrayType ? "*" : "");
        Symbol symbol = new Symbol(declaredType, nameToken.lexeme());
        ASTLocal local = new ASTLocal(currLine(), symbol);

        locals.add(local);
        if (currentMethod != null)
            currentMethod.addLocal(local);

        ASTExpression initializer = null;
        if (tokens.match(T_EQUAL))
            initializer = new ASTExprLocalStore(currLine(), local, expression(), true);

        if (tokens.check(T_COMMA))
            throw new ParseException("Only one typed local declaration is supported in a for initializer.", tokens.current());

        return new ForInitializerDeclaration(List.of(local), initializer);
    }

    private ASTStmtForeach foreachStatement() {
        int line = tokens.previous().line();
        tokens.consume(T_LEFT_PAREN, "Expect '(' after foreach.");

        ASTLocal keyLocal = foreachLocal();
        ASTLocal valueLocal = null;
        if (tokens.match(T_COMMA))
            valueLocal = foreachLocal();

        if (!tokens.match(T_IN) && !tokens.match(T_COLON))
            throw new ParseException("Expect 'in' or ':' in foreach clause.", tokens.current());

        ASTExpression iterable = expression();
        tokens.consume(T_RIGHT_PAREN, "Expect ')' after foreach clause.");

        return new ASTStmtForeach(line, keyLocal, valueLocal, iterable, statement());
    }

    private ASTLocal foreachLocal() {
        Token<String> typeToken = tokens.consume(T_IDENTIFIER, "Expect foreach variable type.");
        boolean isArrayType = tokens.match(TokenType.T_STAR);
        Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect foreach variable name.");
        String declaredType = typeToken.lexeme() + (isArrayType ? "*" : "");
        Symbol symbol = new Symbol(declaredType, nameToken.lexeme());
        ASTLocal local = new ASTLocal(typeToken.line(), symbol);

        locals.add(local);
        if (currentMethod != null)
            currentMethod.addLocal(local);

        return local;
    }

    private ASTStmtExpression expressionStatement() {
        ASTExpression expr = expression();

        tokens.consume(T_SEMICOLON, "Expect ';' after expression.");

        return new ASTStmtExpression(currLine(), expr);
    }

    public ASTExpression expression() {
        return parsePrecedence(PrattParser.Precedence.PREC_ASSIGNMENT);
    }

    private ASTExpression commaExpression() {
        ASTExpression first = expression();
        if (!tokens.match(T_COMMA))
            return first;

        List<ASTExpression> expressions = new ArrayList<>();
        expressions.add(first);

        do {
            expressions.add(expression());
        } while (tokens.match(T_COMMA));

        return new ASTExprSequence(first.line(), expressions);
    }

    public ASTExpression parsePrecedence(int precedence) {
        tokens.advance();

        PrefixParselet pp = PrattParser.getRule(tokens.previous()).prefix();

        if (pp == null)
            throw new ParseException("Expect expression.", tokens.current());

        boolean canAssign = (precedence <= PrattParser.Precedence.PREC_ASSIGNMENT);

        ASTExpression expr = pp.parse(this, canAssign);

        while (precedence <= PrattParser.getRule(tokens.current()).precedence()) {
            tokens.advance();

            InfixParselet ip = PrattParser.getRule(tokens.previous()).infix();

            if (ip == null)
                throw new ParseException("Expect expression.", tokens.current());

            expr = ip.parse(this, expr, canAssign);
        }

        if (canAssign)
            if (tokens.match(T_EQUAL)
                    || tokens.match(T_PLUS_EQUAL)
                    || tokens.match(T_MINUS_EQUAL)
                    || tokens.match(T_STAR_EQUAL)
                    || tokens.match(T_SLASH_EQUAL)
                    || tokens.match(T_PIPE_EQUAL)
                    || tokens.match(T_AMP_EQUAL)
                    || tokens.match(T_CARET_EQUAL)
                    || tokens.match(T_DBL_PIPE_EQUAL)
                    || tokens.match(T_DBL_AMP_EQUAL)
                    || tokens.match(T_LESS_LESS_EQUAL)
                    || tokens.match(T_GREATER_GREATER_EQUAL))
                throw new ParseException("Invalid assignment target.", tokens.current());

        return expr;
    }

    private boolean startsLocalDeclaration() {
        if (!tokens.check(T_IDENTIFIER))
            return false;

        Token<?> next = tokens.peek(1);

        if (next.type() == T_IDENTIFIER)
            return true;

        if (next.type() == TokenType.T_STAR) {
            Token<?> after = tokens.peek(2);
            return after.type() == T_IDENTIFIER;
        }

        return false;
    }

}
