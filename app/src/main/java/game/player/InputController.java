package game.player;

/**
 * 입력을 플레이어에 전달하는 추상화 계층
 * 구현체는 로컬 키보드, 게임패드, 또는 네트워크 소스로부터 명령을 변환해 Player에 전달한다.
 */
public interface InputController {
    /** 바인딩된 플레이어에 입력을 전달하기 시작한다. */
    void bind(Player player);

    /** 바인딩을 해제하고 리소스를 정리한다. */
    void unbind();
}
