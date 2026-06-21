package io.github.protasm.jvmud.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.protasm.jvmud.compiler.efun.builtin.CoreEfuns;
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
