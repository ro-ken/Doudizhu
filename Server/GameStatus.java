
public enum GameStatus {
    WAITING,        /* 等待玩家进入游戏 */
    PREPARATION,    /* 等待玩家确认开始游戏 */
    SELECTING,      /* 发牌阶段并选择地主 */
    RUNNING,        /* 游戏运行阶段 */
    FINISH          /* 游戏结束 */
}
