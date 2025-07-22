package cn.lunadeer.mc.timeManager;

import cn.lunadeer.mc.timeManager.utils.configuration.ConfigurationFile;

public class Configuration extends ConfigurationFile {

    public static boolean debug = false;

    // 插件功能配置
    public static boolean enableTimeCommand = true;

    // 是否在玩家加入时显示插件信息
    public static boolean showWelcomeMessage = true;

    // 欢迎消息内容
    public static String welcomeMessage = "&aTimeManager is enabled! Use &e/ptime help &afor commands.";

}
