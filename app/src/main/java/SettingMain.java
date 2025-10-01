/**
*   설정 세부 메뉴 선택시 호출되는 기능
*   메뉴 이름 String menuName을 받고, view, model, controller를 생성함
*/

public class SettingMain {
    public static SettingView launchSetting(String menuName){
            SettingView view = new SettingView(menuName); //메뉴 이름 넣기 
            SettingModel model = new SettingModel();
            SettingController controller = new SettingController(model, view);
            return view;
    }
}
