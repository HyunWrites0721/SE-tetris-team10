package settings;

/**
*   설정 세부 메뉴 선택시 호출되는 기능
*   메뉴 이름 String menuName을 받고, view, model, controller를 생성함
*/

public class SettingMain {
    public static SettingViewControllerPair launchSetting(String menuName){
            SettingModel model = new SettingModel();
            SettingView view = new SettingView(menuName, model); // model을 view에 전달
            SettingController controller = new SettingController(model, view);
            return new SettingViewControllerPair(view, controller);
    }
    
    // View와 Controller를 함께 반환하기 위한 클래스
    public static class SettingViewControllerPair {
        public final SettingView view;
        public final SettingController controller;
        
        public SettingViewControllerPair(SettingView view, SettingController controller) {
            this.view = view;
            this.controller = controller;
        }
    }
}
