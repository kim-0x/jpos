package factory;

public class RepositoryConfiguration {

    public enum RepositoryType {
        CSV, BIN, JDBC
    }

    private final RepositoryType targetType;

    public RepositoryConfiguration(RepositoryType targetType) {
        this.targetType = targetType;
    }

    public RepositoryFactory createFactory() {
        return switch (targetType) {
            case CSV  -> new CsvRepositoryFactory();
            case BIN  -> new BinRepositoryFactory();
            case JDBC -> new JdbcRepositoryFactory();
        };
    }
}
