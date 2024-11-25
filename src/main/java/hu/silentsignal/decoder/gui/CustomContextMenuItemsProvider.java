package hu.silentsignal.decoder.gui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class CustomContextMenuItemsProvider implements ContextMenuItemsProvider {
    private CustomScannerView customScannerView;

    public CustomContextMenuItemsProvider(CustomScannerView customScannerView) {
        this.customScannerView = customScannerView;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        List<Component> menuItems = new ArrayList<>();
        JMenuItem sendToCustomScanner = new JMenuItem("Send Request to the Decoder");
        menuItems.add(sendToCustomScanner);

        sendToCustomScanner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                customScannerView.setRequestContent(event.messageEditorRequestResponse().get().requestResponse().request());
            }
        });
        return menuItems;
    }
}
