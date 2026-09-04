package io.github.protasm.jvmud.instance;

import io.github.protasm.jvmud.persistence.filesystem.FilesystemAccountStore.Account;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;

/** Filesystem-backed account-login state machine selected explicitly by a mudlib manifest. */
final class FilesystemAccountLoginSession implements ManagedLoginSession {
    private final MudInstance mud;
    private final FilesystemAccountService accounts;
    private final String sessionId;
    private final String remoteAddress;
    private State state = State.ACCOUNT_ID;
    private String accountId = "";
    private String pendingPassword = "";
    private String email = "";
    private String personaName = "";
    private String passwordHash = "";
    private int passwordAttempts;

    FilesystemAccountLoginSession(MudInstance mud, String sessionId, String remoteAddress) {
        this.mud = mud;
        this.accounts = new FilesystemAccountService(mud.mudlibRoot());
        this.sessionId = sessionId;
        this.remoteAddress = remoteAddress;
    }

    @Override
    public void start() {
        message("Please enter your user ID: ");
    }

    @Override
    public boolean noEcho() {
        return state == State.LOGIN_PASSWORD
                || state == State.NEW_PASSWORD
                || state == State.CONFIRM_PASSWORD;
    }

    @Override
    public ManagedLoginResult handle(String line, PrintWriter out) {
        return switch (state) {
            case ACCOUNT_ID -> handleAccountId(line);
            case CREATE_CONFIRMATION -> handleCreateConfirmation(line);
            case LOGIN_PASSWORD -> handleLoginPassword(line, out);
            case NEW_PASSWORD -> handleNewPassword(line);
            case CONFIRM_PASSWORD -> handleConfirmPassword(line);
            case EMAIL -> handleEmail(line);
            case PERSONA_NAME -> handlePersonaName(line);
            case GENDER -> handleGender(line, out);
        };
    }

    private ManagedLoginResult handleAccountId(String line) {
        String normalized = normalizeAccountId(line);
        if (!validAccountId(normalized)) {
            message("Use letters, numbers, underscore, or dash for your user ID.\n");
            message("Please enter your user ID: ");
            return ManagedLoginResult.continueLogin();
        }

        accountId = normalized;
        Optional<Account> account = accounts.load(accountId);
        if (account.isPresent() && !account.orElseThrow().passwordHash().isEmpty()) {
            passwordAttempts = 0;
            passwordHash = account.orElseThrow().passwordHash();
            personaName = account.orElseThrow().personaName();
            email = account.orElseThrow().email();
            message("Password: ");
            state = State.LOGIN_PASSWORD;
            return ManagedLoginResult.continueLogin();
        }

        message("No " + mud.gameName() + " account exists for " + accountId + ". Create it? (yes/no) ");
        state = State.CREATE_CONFIRMATION;
        return ManagedLoginResult.continueLogin();
    }

    private ManagedLoginResult handleCreateConfirmation(String line) {
        String answer = line.toLowerCase();
        if ("yes".equals(answer) || "y".equals(answer)) {
            message("Password: ");
            state = State.NEW_PASSWORD;
            return ManagedLoginResult.continueLogin();
        }
        if ("no".equals(answer) || "n".equals(answer)) {
            message("No account was created. Please visit " + mud.gameName() + " again when you are ready.\n");
            return ManagedLoginResult.disconnectSession();
        }
        message("Please answer yes or no: ");
        return ManagedLoginResult.continueLogin();
    }

    private ManagedLoginResult handleLoginPassword(String line, PrintWriter out) {
        Optional<Account> account = accounts.load(accountId);
        if (account.isPresent() && accounts.verifyPassword(line, account.orElseThrow().passwordHash())) {
            return enter(out, account.orElseThrow());
        }

        passwordAttempts++;
        if (passwordAttempts < 3) {
            message("That password did not match. Please try again.\n");
            message("Password: ");
            return ManagedLoginResult.continueLogin();
        }

        message("That password did not match. Please reconnect when you are ready to try again.\n");
        return ManagedLoginResult.disconnectSession();
    }

    private ManagedLoginResult handleNewPassword(String line) {
        String problem = passwordProblem(line);
        if (problem != null) {
            message(problem + "\n");
            message("Password: ");
            return ManagedLoginResult.continueLogin();
        }

        pendingPassword = line;
        message("Password again: ");
        state = State.CONFIRM_PASSWORD;
        return ManagedLoginResult.continueLogin();
    }

    private ManagedLoginResult handleConfirmPassword(String line) {
        if (!line.equals(pendingPassword)) {
            pendingPassword = "";
            message("Those passwords did not match.\n");
            message("Password: ");
            state = State.NEW_PASSWORD;
            return ManagedLoginResult.continueLogin();
        }

        passwordHash = accounts.hashPassword(line);
        pendingPassword = "";
        message("Email address (optional): ");
        state = State.EMAIL;
        return ManagedLoginResult.continueLogin();
    }

    private ManagedLoginResult handleEmail(String line) {
        if (line.isEmpty()) {
            email = "";
        } else if (!validEmail(line)) {
            message("That email address does not look valid. Enter one address, or leave it blank.\n");
            message("Email address (optional): ");
            return ManagedLoginResult.continueLogin();
        } else {
            email = line;
        }

        message("Persona name: ");
        state = State.PERSONA_NAME;
        return ManagedLoginResult.continueLogin();
    }

    private ManagedLoginResult handlePersonaName(String line) {
        if (!validPersonaName(line)) {
            message("Use 2-24 letters, numbers, spaces, apostrophes, or dashes for your Persona name.\n");
            message("Persona name: ");
            return ManagedLoginResult.continueLogin();
        }

        personaName = capitalize(line.toLowerCase());
        message("Gender (female/male/neutral/none/other): ");
        state = State.GENDER;
        return ManagedLoginResult.continueLogin();
    }

    private ManagedLoginResult handleGender(String line, PrintWriter out) {
        String normalized = line.toLowerCase();
        if (!("female".equals(normalized) || "male".equals(normalized) || "neutral".equals(normalized)
                || "none".equals(normalized) || "other".equals(normalized))) {
            message("Please choose female, male, neutral, none, or other: ");
            return ManagedLoginResult.continueLogin();
        }

        Account account = new Account(accountId, personaName, normalized, email, passwordHash);
        accounts.save(account);
        return enter(out, account);
    }

    private ManagedLoginResult enter(PrintWriter out, Account account) {
        InstancePersona replacement = mud.attachAuthenticatedPersona(
                sessionId,
                out,
                remoteAddress,
                new ManagedPersonaProfile(
                        account.accountId(),
                        account.personaName(),
                        account.gender(),
                        Map.of("email", account.email(), "password_hash", account.passwordHash())));
        return ManagedLoginResult.replaceWith(replacement);
    }

    private void message(String text) {
        mud.messageLoginPlayer(sessionId, text);
    }

    private static String normalizeAccountId(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private static boolean validAccountId(String value) {
        if (value.length() < 3 || value.length() > 24) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (!((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '_' || ch == '-')) {
                return false;
            }
        }
        return true;
    }

    private static String passwordProblem(String value) {
        if (value.length() < 6) {
            return "Password must be at least 6 characters.";
        }
        if (value.length() > 72) {
            return "Password must be 72 characters or fewer.";
        }

        boolean upper = false;
        boolean lower = false;
        boolean number = false;
        boolean special = false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                upper = true;
            } else if (ch >= 'a' && ch <= 'z') {
                lower = true;
            } else if (ch >= '0' && ch <= '9') {
                number = true;
            } else if ("!@#$%^&*_.?+-".indexOf(ch) >= 0) {
                special = true;
            } else {
                return "Password may use letters, numbers, and ! @ # $ % ^ & * _ . ? + - only.";
            }
        }
        if (!upper) {
            return "Password must include an uppercase letter.";
        }
        if (!lower) {
            return "Password must include a lowercase letter.";
        }
        if (!number) {
            return "Password must include a number.";
        }
        if (!special) {
            return "Password must include a special character.";
        }
        return null;
    }

    private static boolean validEmail(String value) {
        int at = value.indexOf('@');
        int dot = value.lastIndexOf('.');
        if (at <= 0 || dot <= at + 1 || dot >= value.length() - 1 || value.indexOf('@', at + 1) >= 0) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (!((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')
                    || (ch >= '0' && ch <= '9') || ch == '@' || ch == '.'
                    || ch == '_' || ch == '%' || ch == '+' || ch == '-')) {
                return false;
            }
        }
        return true;
    }

    private static boolean validPersonaName(String value) {
        if (value.length() < 2 || value.length() > 24) {
            return false;
        }
        boolean sawLetterOrNumber = false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
                sawLetterOrNumber = true;
            } else if (ch != ' ' && ch != '\'' && ch != '-') {
                return false;
            }
        }
        return sawLetterOrNumber;
    }

    private static String capitalize(String value) {
        return value.isEmpty() ? value : value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private enum State {
        ACCOUNT_ID,
        CREATE_CONFIRMATION,
        LOGIN_PASSWORD,
        NEW_PASSWORD,
        CONFIRM_PASSWORD,
        EMAIL,
        PERSONA_NAME,
        GENDER
    }

}
