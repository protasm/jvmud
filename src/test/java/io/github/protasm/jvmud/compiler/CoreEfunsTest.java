package io.github.protasm.jvmud.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.protasm.jvmud.compiler.efun.Efun;
import io.github.protasm.jvmud.compiler.efun.EfunRegistry;
import io.github.protasm.jvmud.compiler.efun.EfunSignature;
import io.github.protasm.jvmud.compiler.efun.builtin.CoreEfuns;
import io.github.protasm.jvmud.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import io.github.protasm.jvmud.compiler.preproc.SearchPathIncludeResolver;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundary;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

final class CoreEfunsTest {
    @Test
    void varargsEfunAcceptsOpenEndedTail() {
        RuntimeContext context = new RuntimeContext(new SearchPathIncludeResolver(Path.of("."), List.of()));
        CoreEfuns.registerCore(context);

        assertNotNull(context.resolveEfun("jvmud_format_text", 30));
        assertEquals("ok", context.invokeEfun("jvmud_format_text", 30, thirtyFormatArgs()));
        assertNotNull(context.resolveEfun("jvmud_capture_session_input", 20));
    }

    @Test
    void exactEfunSignatureBeatsVarargsFallback() {
        EfunRegistry registry = new EfunRegistry();
        Efun fixed = testEfun("probe", List.of(LPCType.LPCINT), null, "fixed");
        Efun varargs = testEfun("probe", List.of(LPCType.LPCINT), LPCType.LPCMIXED, "varargs");
        registry.register(varargs);
        registry.register(fixed);

        assertSame(fixed, registry.lookup("probe", 1));
        assertSame(varargs, registry.lookup("probe", 2));
    }

    @Test
    void ambiguousVarargsEfunSignaturesFailAtLookup() {
        EfunRegistry registry = new EfunRegistry();
        registry.register(testEfun("probe", List.of(LPCType.LPCINT), LPCType.LPCMIXED, "left"));
        registry.register(testEfun("probe", List.of(LPCType.LPCINT), LPCType.LPCMIXED, "right"));

        assertThrows(IllegalStateException.class, () -> registry.lookup("probe", 2));
    }

    @Test
    void dbConnectThreeArgumentEfunKeepsMissingCredentialsNullable() throws SQLException {
        CapturingDriver driver = new CapturingDriver();
        DriverManager.registerDriver(driver);
        try {
            RuntimeContext context = new RuntimeContext(new SearchPathIncludeResolver(Path.of("."), List.of()));
            CoreEfuns.registerCore(context);
            context.setMudlibBoundary(MudlibBoundary.builder()
                    .databaseJdbcUrl(CapturingDriver.URL)
                    .databaseUser("configured-user")
                    .databasePassword("configured-password")
                    .build());

            assertEquals(1, context.invokeEfun("jvmud_db_connect", 3, new Object[] {"RealmsLib", null, null}));

            assertEquals("configured-user", driver.user);
            assertEquals("configured-password", driver.password);
        } finally {
            DriverManager.deregisterDriver(driver);
        }
    }

    private static Object[] thirtyFormatArgs() {
        Object[] args = new Object[30];
        args[0] = "ok";
        for (int i = 1; i < args.length; i++) {
            args[i] = i;
        }
        return args;
    }

    private static Efun testEfun(String name, List<LPCType> parameters, LPCType varargsParameterType, Object result) {
        return new Efun() {
            @Override
            public EfunSignature signature() {
                return new EfunSignature(
                        new Symbol(LPCType.LPCMIXED, name),
                        parameters,
                        varargsParameterType);
            }

            @Override
            public Object call(RuntimeContext context, Object[] args) {
                return result;
            }
        };
    }

    private static final class CapturingDriver implements Driver {
        private static final String URL = "jdbc:jvmud-core-efuns-test";

        private String user;
        private String password;

        @Override
        public Connection connect(String url, Properties info) {
            if (!acceptsURL(url)) {
                return null;
            }
            user = info.getProperty("user");
            password = info.getProperty("password");
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[] {Connection.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "isClosed" -> false;
                        case "close" -> null;
                        case "unwrap" -> null;
                        case "isWrapperFor" -> false;
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
        }

        @Override
        public boolean acceptsURL(String url) {
            return URL.equals(url);
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
            return new DriverPropertyInfo[0];
        }

        @Override
        public int getMajorVersion() {
            return 1;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public boolean jdbcCompliant() {
            return false;
        }

        @Override
        public Logger getParentLogger() {
            return Logger.getGlobal();
        }
    }
}
