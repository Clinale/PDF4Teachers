/*
 * Copyright (c) 2020-2023. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel;

import de.jangassen.MenuToolkit;
import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.menus.EmptyMenu;
import fr.clementgre.pdf4teachers.components.menus.NodeMenu;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.components.menus.NodeRadioMenuItem;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.EditionExporter;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UndoEngine;
import fr.clementgre.pdf4teachers.document.render.convert.ConvertDocument;
import fr.clementgre.pdf4teachers.document.render.display.PageEditPane;
import fr.clementgre.pdf4teachers.document.render.export.ExportWindow;
import fr.clementgre.pdf4teachers.interfaces.CopyPasteManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.booklet.BookletWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.interfaces.windows.log.LogWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.settings.SettingsWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.splitpdf.SplitWindow;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.FilesChooserManager;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ButtonPosition;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.CustomAlert;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.OKAlert;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.WrongAlert;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("serial")
public class MenuBar extends javafx.scene.control.MenuBar {
    
    ////////// ICONS COLOR //////////
    
    public static ColorAdjust colorAdjust = new ColorAdjust();
    
    static{
        if(StyleManager.ACCENT_STYLE == jfxtras.styles.jmetro.Style.DARK) colorAdjust.setBrightness(-0.5);
        else colorAdjust.setBrightness(-1);
    }
    
    ////////// FILE //////////
    
    private final Menu file = new Menu(TR.tr("menuBar.file"));
    public final MenuItem file1Open = createMenuItem(TR.tr("menuBar.file.openFiles"), SVGPathIcons.PDF_FILE, new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.file.openFiles.tooltip"));
    
    public final MenuItem file2OpenDir = createMenuItem(TR.tr("menuBar.file.openDir"), SVGPathIcons.FOLDER, new KeyCodeCombination(KeyCode.O, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.file.openDir.tooltip"));
    
    private final MenuItem file3Clear = createMenuItem(TR.tr("menuBar.file.clearList"), SVGPathIcons.LIST, new KeyCodeCombination(KeyCode.W, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.file.clearList.tooltip"), false, true, false);
    
    private final MenuItem file4Save = createMenuItem(TR.tr("menuBar.file.saveEdit"), SVGPathIcons.SAVE_LITE, new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.file.saveEdit.tooltip"), true, false, false);
    
    private final MenuItem file5Rename = createMenuItem(TR.tr("menuBar.file.renameFile"), SVGPathIcons.COPY, new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.file.renameFile.tooltip"), true, false, false);
    
    private final MenuItem file6Delete = createMenuItem(TR.tr("menuBar.file.deleteEdit"), SVGPathIcons.TRASH, null,
            TR.tr("menuBar.file.deleteEdit.tooltip"), true, false, false);
    
    private final MenuItem file7Close = createMenuItem(TR.tr("menuBar.file.closeDocument"), SVGPathIcons.CROSS, new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.file.closeDocument.tooltip"), true, false, false);
    
    private final MenuItem file8Export = createMenuItem(TR.tr("menuBar.file.export"), SVGPathIcons.EXPORT, new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.file.export.tooltip"), true, false, false);
    
    private final MenuItem file9ExportAll = createMenuItem(TR.tr("menuBar.file.exportAll"), SVGPathIcons.EXPORT, new KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.file.exportAll.tooltip"), false, true, false);
    
    private final MenuItem file10Exit = createMenuItem(TR.tr("menuBar.file.exit"), SVGPathIcons.EXIT, new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN),
            null, false, false, false);
    
    ////////// EDIT //////////
    
    public final Menu edit = new Menu(TR.tr("menuBar.edit"));
    public final MenuItem edit3Cut = createMenuItem(TR.tr("actions.cut"), SVGPathIcons.CUT, CopyPasteManager.KEY_COMB_CUT,
            null, true, false, false);
    public final MenuItem edit4Copy = createMenuItem(TR.tr("actions.copy"), SVGPathIcons.COPY, CopyPasteManager.KEY_COMB_COPY,
            null, true, false, false);
    public final MenuItem edit5Paste = createMenuItem(TR.tr("actions.paste"), SVGPathIcons.PASTE, CopyPasteManager.KEY_COMB_PASTE,
            null, true, false, false);
    private final MenuItem edit1Undo = createMenuItem(TR.tr("actions.undo"), SVGPathIcons.UNDO, UndoEngine.KEY_COMB_UNDO,
            TR.tr("menuBar.edit.undo.tooltip"), true, false, false);
    private final MenuItem edit2Redo = createMenuItem(TR.tr("actions.redo"), SVGPathIcons.REDO, UndoEngine.KEY_COMB_REDO,
            TR.tr("menuBar.edit.redo.tooltip"), true, false, false);
    
    
    ////////// TOOLS //////////
    
    public final Menu tools = new Menu(TR.tr("menuBar.tools"));
    
    private final MenuItem tools1Convert = createMenuItem(TR.tr("menuBar.tools.convertImages"), SVGPathIcons.PICTURES, new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.tools.convertImages.tooltip"), false, false, false);
    
    private final Menu tools3AddPages = createSubMenu(TR.tr("menuBar.tools.addPages"), SVGPathIcons.PLUS,
            TR.tr("menuBar.tools.addPages.tooltip"), true);
    
    private final Menu tools4PdfTools = createSubMenu(TR.tr("menuBar.tools.pdfTools"), SVGPathIcons.WRENCH,
            TR.tr("menuBar.tools.addPages.tooltip"), true);
    
    private final MenuItem tools4PdfTools1Booklet = createMenuItem(TR.tr("bookletWindow.title"), SVGPathIcons.BOOK, null,
            TR.tr("bookletWindow.description"));
    private final MenuItem tools4PdfTools2SplitInterval = createMenuItem(TR.tr("splitPdfWindow.interval.title"), SVGPathIcons.CUT, null,
            TR.tr("splitPdfWindow.description"));
    private final MenuItem tools4PdfTools3SplitColor = createMenuItem(TR.tr("splitPdfWindow.color.title"), SVGPathIcons.CUT, null,
            TR.tr("splitPdfWindow.description"));
    private final MenuItem tools4PdfTools4SplitSelection = createMenuItem(TR.tr("splitPdfWindow.selection.title"), SVGPathIcons.CUT, null,
            TR.tr("splitPdfWindow.selection.description"));
    
    private final MenuItem tools5DeleteAllEdits = createMenuItem(TR.tr("menuBar.tools.deleteAllEdits"), SVGPathIcons.TRASH, null,
            TR.tr("menuBar.tools.deleteAllEdits.tooltip"));
    
    private final Menu tools6SameNameEditions = createSubMenu(TR.tr("menuBar.tools.sameNameEdits"), SVGPathIcons.EXCHANGE,
            TR.tr("menuBar.tools.sameNameEdits.tooltip"), true);
    private final MenuItem tools6SameNameEditionsNull = createMenuItem(TR.tr("menuBar.tools.sameNameEdits.noEditFounded"), null);
    
    private final Menu tools6ExportImportEdition = createSubMenu(TR.tr("menuBar.tools.exportOrImportEditOrGradeScale"), SVGPathIcons.EXPORT,
            TR.tr("menuBar.tools.exportOrImportEditOrGradeScale.tooltip"), true);
    
    private final MenuItem tools7ExportEdition1All = createMenuItem(TR.tr("menuBar.tools.exportEdit"), null, null,
            TR.tr("menuBar.tools.exportEdit.tooltip"), true, false, false);
    private final MenuItem tools7ExportEdition2Grades = createMenuItem(TR.tr("menuBar.tools.exportGradeScale"), null, null,
            TR.tr("menuBar.tools.exportGradeScale.tooltip"), true, false, false);
    
    private final MenuItem tools7ImportEdition1All = createMenuItem(TR.tr("menuBar.tools.importEdit"), null, null,
            TR.tr("menuBar.tools.importEdit.tooltip"), true, false, false);
    private final MenuItem tools7ImportEdition2Grades = createMenuItem(TR.tr("menuBar.tools.importGradeScale"), null, null,
            TR.tr("menuBar.tools.importGradeScale.tooltip"), true, false, false);
    
    private final MenuItem tools8FullScreen = createMenuItem(TR.tr("menuBar.tools.fullScreenMode"), SVGPathIcons.FULL_SCREEN, new KeyCodeCombination(KeyCode.F11),
            TR.tr("menuBar.tools.fullScreenMode.tooltip"));
    
    private final Menu tools8Debug = createSubMenu(TR.tr("menuBar.tools.debug"), SVGPathIcons.COMMAND_PROMPT,
            TR.tr("menuBar.tools.debug.tooltip"), false);
    
    private final MenuItem tools8Debug1OpenConsole = createMenuItem(TR.tr("menuBar.tools.debug.openPrintStream"), null, new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN, KeyCombination.SHORTCUT_DOWN),
            TR.tr("menuBar.tools.debug.openPrintStream.tooltip"), false, false, false);
    private final MenuItem tools8Debug2OpenAppFolder = createMenuItem(TR.tr("menuBar.tools.debug.openDataFolder"), null, null,
            TR.tr("menuBar.tools.debug.openDataFolder.tooltip"), false, false, false);
    private final MenuItem tools8Debug3OpenEditionFile = createMenuItem(TR.tr("menuBar.tools.debug.openCurrentEditFile"), null, null,
            TR.tr("menuBar.tools.debug.openCurrentEditFile.tooltip"), true, false, false);
    
    ////////// HELP //////////
    
    private final Menu help = new Menu(TR.tr("menuBar.help"));
    private final MenuItem help1LoadDoc = createMenuItem(TR.tr("menuBar.help.loadDocumentation"), SVGPathIcons.INFO);
    private final MenuItem help2GitHubIssue = createMenuItem(TR.tr("menuBar.help.gitHubIssue"), SVGPathIcons.GITHUB);
    private final MenuItem help3Twitter = createMenuItem(TR.tr("menuBar.help.twitter"), SVGPathIcons.TWITTER);
    private final MenuItem help4Website = createMenuItem(TR.tr("menuBar.help.website"), SVGPathIcons.GLOBE);
    
    ////////// EMPTY MENUS //////////
    
    public EmptyMenu settings = new EmptyMenu(TR.tr("menuBar.settings"), this);
    public EmptyMenu about = new EmptyMenu(TR.tr("menuBar.about"), this);
    
    
    public MenuBar(){
        setup();
    }
    
    public static boolean isSystemMenuBarSupported(){
        return PlatformUtils.isMac();
    }
    public void setup(){
        if(isSystemMenuBarSupported()) setUseSystemMenuBar(true);
        
        ////////// FILE //////////
        
        if(PlatformUtils.isMac()){
            file.getItems().addAll(file1Open, file2OpenDir, file3Clear, new SeparatorMenuItem(), file4Save, file5Rename, file6Delete, file7Close, new SeparatorMenuItem(), file8Export, file9ExportAll);
        }else{
            file.getItems().addAll(file1Open, file2OpenDir, file3Clear, new SeparatorMenuItem(), file4Save, file5Rename, file6Delete, file7Close, new SeparatorMenuItem(), file8Export, file9ExportAll, new SeparatorMenuItem(), file10Exit);
        }
        
        ////////// EDIT //////////
        
        edit.getItems().addAll(edit1Undo, edit2Redo, new SeparatorMenuItem(), edit3Cut, edit4Copy, edit5Paste);
        
        ////////// TOOLS //////////
        
        tools3AddPages.getItems().add(new MenuItem(""));
        tools4PdfTools.getItems().addAll(tools4PdfTools1Booklet, tools4PdfTools2SplitInterval, tools4PdfTools3SplitColor, tools4PdfTools4SplitSelection);
        tools6ExportImportEdition.getItems().addAll(tools7ExportEdition1All, tools7ExportEdition2Grades, tools7ImportEdition1All, tools7ImportEdition2Grades);
        tools6SameNameEditions.getItems().add(tools6SameNameEditionsNull);
        tools8Debug.getItems().add(tools8Debug1OpenConsole);
        tools8Debug.getItems().addAll(tools8Debug2OpenAppFolder, tools8Debug3OpenEditionFile);
        
        tools.getItems().addAll(tools1Convert, /*tools2QRCode,*/ tools3AddPages, tools4PdfTools,
                new SeparatorMenuItem(), tools5DeleteAllEdits, tools6SameNameEditions, tools6ExportImportEdition,
                new SeparatorMenuItem(), tools8FullScreen,
                new SeparatorMenuItem(), tools8Debug);
        
        ////////// HELP //////////
        
        help.getItems().addAll(help1LoadDoc, help2GitHubIssue, help3Twitter, help4Website);
        
        ////////// FILE //////////
        
        file1Open.setOnAction((ActionEvent actionEvent) -> {
            
            File[] files = FilesChooserManager.showPDFFilesDialog(FilesChooserManager.SyncVar.LAST_OPEN_DIR);
            if(files != null){
                MainWindow.filesTab.openFiles(files);
                if(files.length == 1){
                    MainWindow.mainScreen.openFile(files[0]);
                }
            }
        });
        file2OpenDir.setOnAction((ActionEvent actionEvent) -> {
            
            File directory = FilesChooserManager.showDirectoryDialog(FilesChooserManager.SyncVar.LAST_OPEN_DIR);
            if(directory != null){
                MainWindow.filesTab.openFiles(new File[]{directory});
            }
        });
        file3Clear.setOnAction((ActionEvent actionEvent) -> {
            MainWindow.filesTab.clearFiles();
        });
        file4Save.setOnAction((ActionEvent actionEvent) -> {
            if(MainWindow.mainScreen.hasDocument(true)){
                MainWindow.mainScreen.document.edition.save(true);
            }
        });
        file5Rename.setOnAction(e -> {
            if(MainWindow.mainScreen.hasDocument(true)){
                MainWindow.filesTab.requestFileRename(MainWindow.mainScreen.document.getFile());
            }
        });
        file6Delete.setOnAction((ActionEvent e) -> {
            if(MainWindow.mainScreen.hasDocument(true)){
                MainWindow.mainScreen.document.edition.clearEdit(true);
            }
        });
        file7Close.setOnAction((ActionEvent e) -> {
            if(MainWindow.mainScreen.hasDocument(true)){
                MainWindow.mainScreen.closeFile(true, false);
            }
        });
        file8Export.setOnAction((ActionEvent actionEvent) -> {
            
            if(!MainWindow.mainScreen.document.save(true)) return;
            new ExportWindow(Collections.singletonList(MainWindow.mainScreen.document.getFile()));
            
        });
        file9ExportAll.setOnAction((ActionEvent actionEvent) -> {
            
            if(MainWindow.mainScreen.hasDocument(false)){
                if(!MainWindow.mainScreen.document.save(true)) return;
            }
            new ExportWindow(MainWindow.filesTab.files.getItems());
            
        });
        file10Exit.setOnAction(e -> MainWindow.requestCloseApp());
        
        ////////// EDIT //////////
        
        edit.addEventHandler(Menu.ON_SHOWING, (e) -> {
            String nextUndo = null;
            String nextRedo = null;
            if(MainWindow.mainScreen.getUndoEngineAuto() != null){
                nextUndo = MainWindow.mainScreen.getUndoEngineAuto().getUndoNextName();
                nextRedo = MainWindow.mainScreen.getUndoEngineAuto().getRedoNextName();
            }
            if(nextUndo != null) nextUndo = TR.tr("actions.undo") + " \"" + nextUndo + "\"";
            else nextUndo = TR.tr("actions.undo") + " (" + TR.tr("actions.undo.nothingToUndo") + ")";
            
            if(nextRedo != null) nextRedo = TR.tr("actions.redo") + " \"" + nextRedo + "\"";
            else nextRedo = TR.tr("actions.redo") + " (" + TR.tr("actions.redo.nothingToRedo") + ")";
            
            if(edit1Undo instanceof NodeMenuItem menu) menu.setName(nextUndo);
            else edit1Undo.setText(nextUndo);
            if(edit2Redo instanceof NodeMenuItem menu) menu.setName(nextRedo);
            else edit2Redo.setText(nextRedo);
            
            
            String cut = TR.tr("actions.cut");
            String copy = TR.tr("actions.copy");
            if(CopyPasteManager.doNodeCanPerformAction(Main.window.getScene().getFocusOwner(), CopyPasteManager.CopyPasteType.COPY)){
                cut = TR.tr("actions.cutSelectedText");
                copy = TR.tr("actions.copySelectedText");
            }else if(MainWindow.mainScreen.hasDocument(false) && MainWindow.mainScreen.getSelected() != null){
                cut = TR.tr("actions.cutElement");
                copy = TR.tr("actions.copyElement");
            }
            if(edit3Cut instanceof NodeMenuItem menu) menu.setName(cut);
            else edit3Cut.setText(cut);
            if(edit4Copy instanceof NodeMenuItem menu) menu.setName(copy);
            else edit4Copy.setText(copy);
            
            if(CopyPasteManager.doNodeCanPerformAction(Main.window.getScene().getFocusOwner(), CopyPasteManager.CopyPasteType.PASTE)){
            
            }else if(Element.ELEMENT_CLIPBOARD_KEY.equals(Clipboard.getSystemClipboard().getContent(Main.INTERNAL_FORMAT)) && Element.elementClipboard != null){
            
            }
            
            
            String paste = TR.tr("actions.paste");
            if(CopyPasteManager.doNodeCanPerformAction(Main.window.getScene().getFocusOwner(), CopyPasteManager.CopyPasteType.PASTE)){
                paste = TR.tr("actions.pasteClipboardString");
            }else if(Element.ELEMENT_CLIPBOARD_KEY.equals(Clipboard.getSystemClipboard().getContent(Main.INTERNAL_FORMAT)) && Element.elementClipboard != null){
                paste = TR.tr("actions.paste") + " (" + Element.elementClipboard.getElementName(false) + ")";
            }
            if(edit5Paste instanceof NodeMenuItem menu) menu.setName(paste);
            else edit5Paste.setText(paste);
        });
        
        edit1Undo.setOnAction(e -> MainWindow.mainScreen.undo());
        edit2Redo.setOnAction(e -> MainWindow.mainScreen.redo());
        
        edit3Cut.setOnAction(e -> CopyPasteManager.execute(CopyPasteManager.CopyPasteType.CUT));
        edit4Copy.setOnAction(e -> CopyPasteManager.execute(CopyPasteManager.CopyPasteType.COPY));
        edit5Paste.setOnAction(e -> CopyPasteManager.execute(CopyPasteManager.CopyPasteType.PASTE));
        
        ////////// TOOLS //////////
        
        tools1Convert.setOnAction(e -> new ConvertDocument());
        
        tools3AddPages.setOnShowing(e -> {
            tools3AddPages.getItems().setAll(PageEditPane.getNewPageMenu(0, 0, MainWindow.mainScreen.document.numberOfPages, true, isSystemMenuBarSupported()));
            NodeMenuItem.setupMenu(tools3AddPages);
        });
        
        tools4PdfTools1Booklet.setOnAction(e -> new BookletWindow());
        tools4PdfTools2SplitInterval.setOnAction(e -> {
            MainWindow.mainScreen.setIsEditPagesMode(true);
            new SplitWindow(SplitWindow.SplitType.INTERVAL);
        });
        tools4PdfTools3SplitColor.setOnAction(e -> {
            MainWindow.mainScreen.setIsEditPagesMode(true);
            new SplitWindow(SplitWindow.SplitType.COLOR);
        });
        tools4PdfTools4SplitSelection.setOnAction(e -> {
            MainWindow.mainScreen.setIsEditPagesMode(true);
            if(MainWindow.mainScreen.document.getSelectedPages().isEmpty() || MainWindow.mainScreen.document.getSelectedPages().size() == MainWindow.mainScreen.document.numberOfPages){
                new WrongAlert(TR.tr("splitPdfWindow.error.noSelectedPages.header"), TR.tr("splitPdfWindow.error.noSelectedPages.description"), false).execute();
            }else new SplitWindow(SplitWindow.SplitType.SELECTION);
        });
        
        tools5DeleteAllEdits.setOnAction((ActionEvent e) -> {
            CustomAlert dialog = new CustomAlert(Alert.AlertType.WARNING, TR.tr("dialog.deleteEdits.confirmation.title"), TR.tr("dialog.deleteEdits.confirmation.header"));
            
            float yesButSize = FilesUtils.convertBytesToMegaBytes(FilesUtils.getSize(new File(Main.dataFolder + "editions").toPath()));
            float yesSize = 0L;
            for(File file : MainWindow.filesTab.files.getItems()){
                File editFile = Edition.getEditFile(file);
                yesSize += FilesUtils.getSize(editFile.toPath());
            }
            yesSize = FilesUtils.convertBytesToMegaBytes((long) yesSize);
            
            dialog.addNoButton(ButtonPosition.CLOSE);
            dialog.addButton(TR.tr("actions.yes") + " (" + yesSize + "Mi" + TR.tr("data.byte") + ")", ButtonPosition.DEFAULT);
            dialog.addButton(TR.tr("dialog.deleteEdits.confirmation.buttons.deleteAll") + " (" + yesButSize + "Mi" + TR.tr("data.byte") + ")", ButtonPosition.OTHER_RIGHT);
            
            ButtonPosition option = dialog.getShowAndWaitGetButtonPosition(ButtonPosition.CLOSE);
            float size;
            if(option == ButtonPosition.DEFAULT){
                if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.document.edition.clearEdit(false);
                for(File file : MainWindow.filesTab.files.getItems()) Edition.getEditFile(file).delete();
                size = yesSize;
            }else if(option == ButtonPosition.OTHER_RIGHT){
                if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.document.edition.clearEdit(false);
                for(File file : Objects.requireNonNull(new File(Main.dataFolder + "editions").listFiles()))
                    file.delete();
                size = yesButSize;
            }else return;
            
            new OKAlert(TR.tr("dialog.deleteEdits.completed.title"),
                    TR.tr("dialog.deleteEdits.completed.header"), TR.tr("dialog.deleteEdits.completed.details", String.valueOf(size))).show();
        });
        tools6SameNameEditions.setOnShowing((Event event) -> {
            tools6SameNameEditions.getItems().clear();
            int i = 0;
            for(Map.Entry<File, File> files : Edition.getEditFilesWithSameName(MainWindow.mainScreen.document.getFile()).entrySet()){
                
                MenuItem item = createMenuItem(files.getValue().getAbsolutePath(), null);
                if(files.getValue().getParentFile() != null){
                    item.setText(files.getValue().getParentFile().getAbsolutePath().replace(System.getProperty("user.home"), "~") + File.separator);
                }
                
                
                tools6SameNameEditions.getItems().add(item);
                item.setOnAction((ActionEvent actionEvent) -> {
                    CustomAlert dialog = new CustomAlert(Alert.AlertType.CONFIRMATION, TR.tr("dialog.importEdit.confirm.title"), TR.tr("dialog.loadSameNameEdit.confirmation.header"));
                    
                    dialog.addNoButton(ButtonPosition.CLOSE);
                    dialog.addYesButton(ButtonPosition.DEFAULT);
                    dialog.addButton(TR.tr("dialog.loadSameNameEdit.confirmation.buttons.yesForAllSameFolder"), ButtonPosition.OTHER_RIGHT);
                    
                    ButtonPosition option = dialog.getShowAndWaitGetButtonPosition(ButtonPosition.CLOSE);
                    if((option == ButtonPosition.DEFAULT || option == ButtonPosition.OTHER_RIGHT) && MainWindow.mainScreen.hasDocument(true)){
                        
                        // Opened document
                        MainWindow.mainScreen.document.edition.clearEdit(false);
                        Edition.mergeEditFileWithEditFile(files.getKey(), Edition.getEditFile(MainWindow.mainScreen.document.getFile()));
                        MainWindow.mainScreen.document.loadEdition(false);
                        
                        // Other documents of the same folder in the list
                        if(option == ButtonPosition.OTHER_RIGHT){
                            
                            for(File otherFileDest : MainWindow.filesTab.files.getItems()){
                                if(otherFileDest.getParentFile().getAbsolutePath().equals(MainWindow.mainScreen.document.getFile().getParentFile().getAbsolutePath()) && !otherFileDest.equals(MainWindow.mainScreen.document.getFile())){
                                    File fromEditFile = Edition.getEditFile(new File(files.getValue().getParentFile().getAbsolutePath() + "/" + otherFileDest.getName()));
                                    
                                    if(fromEditFile.exists()){
                                        Edition.mergeEditFileWithEditFile(fromEditFile, Edition.getEditFile(otherFileDest));
                                    }else{
                                        WrongAlert alert = new WrongAlert(TR.tr("dialog.loadSameNameEdit.fileNotFound.title"),
                                                TR.tr("dialog.loadSameNameEdit.fileNotFound.header", otherFileDest.getName(), FilesUtils.getPathReplacingUserHome(files.getValue().getParentFile().toPath())), true);
                                        if(alert.execute()) return;
                                    }
                                }
                            }
                        }
                    }
                });
                i++;
            }
            if(i == 0) tools6SameNameEditions.getItems().add(tools6SameNameEditionsNull);
            else NodeMenuItem.setupMenu(tools6SameNameEditions);
        });
        
        tools7ExportEdition1All.setOnAction((e) -> EditionExporter.showExportDialog(false));
        tools7ExportEdition2Grades.setOnAction((e) -> EditionExporter.showExportDialog(true));
        tools7ImportEdition1All.setOnAction((e) -> EditionExporter.showImportDialog(false));
        tools7ImportEdition2Grades.setOnAction((e) -> EditionExporter.showImportDialog(true));
        
        tools8FullScreen.setOnAction((e) -> Main.window.setFullScreen(!Main.window.isFullScreen()));
        
        tools8Debug1OpenConsole.setOnAction((e) -> new LogWindow());
        tools8Debug2OpenAppFolder.setOnAction((e) -> PlatformUtils.openFile(Main.dataFolder));
        tools8Debug3OpenEditionFile.setOnAction((e) -> {
            File file = Edition.getEditFile(MainWindow.mainScreen.document.getFile());
            if(!file.exists()){
                try{
                    file.createNewFile();
                }catch(IOException ex){
                    Log.eNotified(ex);
                }
            }
            PlatformUtils.openFile(file.getAbsolutePath());
        });
        
        ////////// ABOUT / HELP //////////
        
        help1LoadDoc.setOnAction((ActionEvent actionEvent) -> MainWindow.mainScreen.openFile(TR.getDocFile()));
        help2GitHubIssue.setOnAction((ActionEvent actionEvent) -> {
            try{
                Desktop.getDesktop().browse(new URI("https://github.com/themsou/PDF4Teachers/issues/new"));
            }catch(IOException | URISyntaxException e){
                Log.eNotified(e);
            }
        });
        help3Twitter.setOnAction((ActionEvent t) -> Main.hostServices.showDocument("https://twitter.com/PDF4Teachers"));
        help4Website.setOnAction((ActionEvent t) -> Main.hostServices.showDocument("https://pdf4teachers.org"));
        
        ////////// END PROCESS - OSX ADAPTION & MENU //////////
        
        // UI Style
        setStyle("");
        StyleManager.putStyle(this, Style.ACCENT);
        
        if(isSystemMenuBarSupported()){
            
            if(PlatformUtils.isMac()){
                getMenus().addAll(file, edit, tools, help);
                
                MenuToolkit tk = MenuToolkit.toolkit(TR.locale);
                
                MenuItem about = tk.createAboutMenuItem("");
                about.setText(TR.tr("menuBar.osx.about", Main.APP_NAME));
                about.setOnAction((e) -> Main.showAboutWindow());
                
                MenuItem settings = tk.createAboutMenuItem("", (e) -> new SettingsWindow());
                settings.setText(TR.tr("menuBar.settings"));
                settings.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.META_DOWN));
                
                MenuItem hide = tk.createHideMenuItem("");
                hide.setText(TR.tr("menuBar.osx.hide", Main.APP_NAME));
                
                MenuItem hideOthers = tk.createHideOthersMenuItem();
                hideOthers.setText(TR.tr("menuBar.osx.hideOthers"));
                
                MenuItem unhideAll = tk.createUnhideAllMenuItem();
                unhideAll.setText(TR.tr("menuBar.osx.unhideAll"));
                
                MenuItem quit = tk.createQuitMenuItem("");
                quit.setText(TR.tr("menuBar.osx.quit", Main.APP_NAME));
                quit.setOnAction((e) -> {
                    if(!MainWindow.requestCloseApp()) e.consume();
                });
                
                Menu defaultApplicationMenu = new Menu(Main.APP_NAME, null,
                        about, new SeparatorMenuItem(), settings, new SeparatorMenuItem(),
                        hide, hideOthers, unhideAll, new SeparatorMenuItem(), quit);
                tk.setApplicationMenu(defaultApplicationMenu);
            }
            
        }else{
            settings.setOnClick(e -> new SettingsWindow());
            about.setOnClick(e -> Main.showAboutWindow());
            
            NodeMenuItem.setupMenu(file);
            NodeMenuItem.setupMenu(tools);
            NodeMenuItem.setupMenu(tools6ExportImportEdition);
            NodeMenuItem.setupMenu(tools8Debug);
            NodeMenuItem.setupMenu(help);
            // edit is edited dynamic
            NodeMenuItem.setupDynamicMenu(edit);
            
            getMenus().addAll(file, edit, tools, help, settings, about);
            
            setupMenus();
            Main.settings.menuForceOpen.valueProperty().addListener((observable, oldValue, newValue) -> {
                setupMenus();
            });
        }
        
    }
    
    public void setupMenus(){
        for(Menu menu : getMenus()){
            if(!menu.getItems().isEmpty()){
                menu.setStyle("-fx-padding: 5 7 5 7;");
                if(Main.settings.menuForceOpen.getValue()){
                    menu.setOnShowing((e) -> {
                        for(int i = 50; i <= 500; i += 50){
                            PlatformUtils.runLaterOnUIThread(i, () -> {
                                for(Menu m : getMenus()){
                                    if(m.isShowing()) return;
                                }
                                menu.show();
                            });
                        }
                    });
                }else{
                    menu.setOnShowing(null);
                }
            }else menu.setStyle("-fx-padding: 0;");
        }
    }
    
    
    public static Menu createSubMenu(String name, String image, String toolTip, boolean disableIfNoDoc){
        
        Menu menu;
        if(isSystemMenuBarSupported()){
            menu = new Menu(name);
        }else{
            menu = new NodeMenu(name);
            
            if(image != null){
                if(image.length() >= 30){
                    ((NodeMenu) menu).setImage(SVGPathIcons.generateImage(image, "white", 0, 16, colorAdjust));
                }else{
                    if(MenuBar.class.getResource("/img/MenuBar/" + image + ".png") == null)
                        Log.e("MenuBar image " + image + " does not exist");
                    else
                        ((NodeMenu) menu).setImage(ImageUtils.buildImage(MenuBar.class.getResource("/img/MenuBar/" + image + ".png") + "", 0, 0, colorAdjust));
                }
            }
            if(toolTip != null && !toolTip.isBlank()) ((NodeMenu) menu).setToolTip(toolTip);
        }
        setupAutomaticDisabling(disableIfNoDoc, false, menu);
        return menu;
    }
    
    public static MenuItem createRadioMenuItem(String text, String image, String toolTip, boolean autoUpdate){
        
        if(isSystemMenuBarSupported()){
            RadioMenuItem menuItem = new RadioMenuItem(text);
            //if(imgName != null) menuItem.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/MenuBar/"+ imgName + ".png")+"", 0, 0));
            
            //OSX selects radioMenuItems upon click, but doesn't unselect it on click :
            AtomicBoolean selected = new AtomicBoolean(false);
            menuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    selected.set(newValue);
                });
            });
            menuItem.setOnAction((e) -> {
                menuItem.setSelected(!selected.get());
            });
            
            return menuItem;
            
        }else{
            NodeRadioMenuItem menuItem = new NodeRadioMenuItem(text + "      ", autoUpdate, true);
            
            if(image != null){
                if(image.length() >= 30){
                    menuItem.setImage(SVGPathIcons.generateImage(image, "white", 0, 16, colorAdjust));
                }else{
                    if(MenuBar.class.getResource("/img/MenuBar/" + image + ".png") == null)
                        Log.e("MenuBar image " + image + " does not exist");
                    else
                        menuItem.setImage(ImageUtils.buildImage(MenuBar.class.getResource("/img/MenuBar/" + image + ".png") + "", 0, 0, colorAdjust));
                }
                
            }
            if(!toolTip.isBlank()) menuItem.setToolTip(toolTip);
            
            return menuItem;
        }
        
        
    }
    
    public static MenuItem createMenuItem(String text, String image, KeyCombination keyCombinaison, String toolTip, boolean disableIfNoDoc, boolean disableIfNoList, boolean leftMargin){
        if(keyCombinaison != null) MainWindow.keyboardShortcuts.registerMenuBarShortcut(keyCombinaison, text);
        if(isSystemMenuBarSupported()){
            MenuItem menuItem = new MenuItem(text);
            //if(imgName != null) menuItem.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/MenuBar/"+ imgName + ".png")+"", 0, 0));
            if(keyCombinaison != null){
                if(!PlatformUtils.isMac() || !keyCombinaison.equals(new KeyCodeCombination(KeyCode.F11))) // Exclude the F11 combinaison on Mac OS
                    menuItem.setAccelerator(keyCombinaison);
            }
            setupAutomaticDisabling(disableIfNoDoc, disableIfNoList, menuItem);
            return menuItem;
        }else{
            NodeMenuItem menuItem = new NodeMenuItem(text, false);
            
            if(image != null) menuItem.setImage(SVGPathIcons.generateImage(image, "white", 0, 16, colorAdjust));
            
            if(keyCombinaison != null) menuItem.setKeyCombinaison(keyCombinaison);
            if(toolTip != null && !toolTip.isBlank()) menuItem.setToolTip(toolTip);
            if(leftMargin) menuItem.setFalseLeftData();
            
            setupAutomaticDisabling(disableIfNoDoc, disableIfNoList, menuItem);
            return menuItem;
        }
    }
    private static void setupAutomaticDisabling(boolean disableIfNoDoc, boolean disableIfNoList, MenuItem menuItem){
        if(disableIfNoDoc){
            menuItem.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.statusProperty().get() != MainScreen.Status.OPEN, MainWindow.mainScreen.statusProperty()));
        }
        if(disableIfNoList){
            menuItem.disableProperty().bind(Bindings.size(MainWindow.filesTab.getOpenedFiles()).isEqualTo(0));
        }
    }
    
    public static MenuItem createMenuItem(String text, String imgName, KeyCombination keyCombinaison, String toolTip){
        return createMenuItem(text, imgName, keyCombinaison, toolTip, false, false, false);
    }
    
    public static MenuItem createMenuItem(String text, String imgName, KeyCombination keyCombinaison, String toolTip, boolean leftMargin){
        return createMenuItem(text, imgName, keyCombinaison, toolTip, false, false, leftMargin);
    }
    public static MenuItem createMenuItem(String text, String imgName){
        return createMenuItem(text, imgName, null, "", false, false, false);
    }
}
