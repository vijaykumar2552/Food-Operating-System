// Launches the FoodOS desktop application.
// Initializes the user interface and its application entry flow.

package com.foodapp;

import com.foodapp.config.AppConfig;
import com.foodapp.dao.JdbcUserDao;
import com.foodapp.service.AuthenticationService;
import com.foodapp.util.Database;
import com.foodapp.view.LoginFrame;
import com.foodapp.view.UiTheme;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() { }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UiTheme.apply();
            AuthenticationService authenticationService = new AuthenticationService(new Database(AppConfig.load()), new JdbcUserDao());
            new LoginFrame(authenticationService).setVisible(true);
        });
    }
}
