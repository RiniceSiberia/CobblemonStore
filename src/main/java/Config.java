import com.xxxt.cobblemon_store.CobblemonStore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = CobblemonStore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<String> MYSQL_PATH = BUILDER
            .define("mysql_path", "jdbc:mysql://localhost:3306/");

    private static final ModConfigSpec.ConfigValue<String> MYSQL_DRIVER = BUILDER
            .define("mysql_driver", "com.mysql.cj.jdbc.Driver");

    private static final ModConfigSpec.ConfigValue<String> MYSQL_USER = BUILDER
            .define("mysql_user", "root");

    private static final ModConfigSpec.ConfigValue<String> MYSQL_PASSWORD = BUILDER
            .define("mysql_password", "123456");

    private static final ModConfigSpec.ConfigValue<String> DB_NAME = BUILDER
            .define("db_name", "mydb");

    private static final ModConfigSpec.ConfigValue<String> EXTRA_OPTIONS = BUILDER
            .define("extra_options", "?useSSL=false&serverTimezone=UTC");


    static final ModConfigSpec SPEC = BUILDER.build();

    public static String mysql_path;
    public static String mysql_driver;
    public static String mysql_user;
    public static String mysql_password;
    public static String db_name;
    public static String extra_options;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        mysql_path = MYSQL_PATH.get();
        mysql_driver = MYSQL_DRIVER.get();
        mysql_user = MYSQL_USER.get();
        mysql_password = MYSQL_PASSWORD.get();
        db_name = DB_NAME.get();
        extra_options = EXTRA_OPTIONS.get();
    }
}
