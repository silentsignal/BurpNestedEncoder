package hu.silentsignal.decoder;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.utilities.Utilities;
import hu.silentsignal.decoder.gui.CustomContextMenuItemsProvider;
import hu.silentsignal.decoder.gui.CustomScannerView;

public class Main implements BurpExtension {

    MontoyaApi api;
    Logging logging;
    Utilities utils;
    CustomScannerView customScannerView;

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        api = montoyaApi;
        logging = api.logging();
        utils = api.utilities();
        api.extension().setName("Custom Decoder with GUI");
        customScannerView = new CustomScannerView(api);
        // If you only wish to use the traditional Insertion Point Provider functionality
        // Without the GUI, uncomment the code below
        //api.scanner().registerInsertionPointProvider(new CustomAuditInsertionPointProvider(api));
        // Registering the context menu
        api.userInterface().registerContextMenuItemsProvider(new CustomContextMenuItemsProvider(customScannerView));
        // Registering the main GUI
        api.userInterface().registerSuiteTab("Custom Decoder", customScannerView);
    }
}