package cn.lunadeer.mc.timeEssence;

import cn.lunadeer.mc.timeEssence.utils.configuration.ConfigurationFile;

public class Configuration extends ConfigurationFile {

    public static boolean debug = false;

    // 插件功能配置
    public static boolean enableTimeCommand = true;

    // 时间更新间隔（tick，20 ticks = 1秒）
    public static int timeUpdateInterval = 20;

    // 是否在玩家加入时显示插件信息
    public static boolean showWelcomeMessage = true;

    // 欢迎消息内容
    public static String welcomeMessage = "&aTimeEssence is enabled! Use &e/time help &afor commands.";

}
