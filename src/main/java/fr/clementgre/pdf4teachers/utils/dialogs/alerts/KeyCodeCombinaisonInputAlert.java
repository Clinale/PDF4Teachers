/*
 * Copyright (c) 2023. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.dialogs.alerts;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.input.*;

public class KeyCodeCombinaisonInputAlert extends TextInputAlert {
    
    private KeyCharacterCombination combinaison;
    private KeyCode keyCode = null;
    private boolean hasReleased = true;
    private final boolean requireShortcutKey;
    
    public enum Result {
        CANCEL, DELETE, VALIDATE
    }
    
    public KeyCodeCombinaisonInputAlert(String title, String header, KeyCodeCombination defaultCombinaison, boolean requireShortcutKey, boolean removeShortcutOption){
        super(title, header, TR.tr("dialogs.keyCodeCombinaisonInput.details"));
        this.requireShortcutKey = requireShortcutKey;
        
        if(defaultCombinaison == null) this.combinaison = new KeyCharacterCombination("");
        else{
            this.keyCode = defaultCombinaison.getCode();
            this.combinaison = new KeyCharacterCombination("", defaultCombinaison.getShift(), defaultCombinaison.getControl(),
                    defaultCombinaison.getAlt(), defaultCombinaison.getMeta(), defaultCombinaison.getShortcut());
        }
        
        updateText();
        
        super.input.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if(hasReleased){
                // Reset the combinaison
                hasReleased = false;
                if(e.getCode() != keyCode){ // Events repeats automatically
                    combinaison = new KeyCharacterCombination("");
                    keyCode = null;
                }
            }
            
            
            if(e.getCode() != KeyCode.UNDEFINED && e.getCode() != KeyCode.SHIFT && e.getCode() != KeyCode.CONTROL
                    && e.getCode() != KeyCode.ALT && e.getCode() != KeyCode.META && e.getCode() != KeyCode.COMMAND && e.getCode() != KeyCode.SHORTCUT){
                keyCode = e.getCode();
            }else if(e.isShiftDown() && combinaison.getShift() != KeyCombination.ModifierValue.DOWN){
                combinaison = new KeyCharacterCombination("", KeyCombination.ModifierValue.DOWN, combinaison.getControl(),
                        combinaison.getAlt(), combinaison.getMeta(), combinaison.getShortcut());
            }else if(e.isControlDown() && combinaison.getControl() != KeyCombination.ModifierValue.DOWN){
                combinaison = new KeyCharacterCombination("", combinaison.getShift(), KeyCombination.ModifierValue.DOWN,
                        combinaison.getAlt(), combinaison.getMeta(), combinaison.getShortcut());
            }else if(e.isAltDown() && combinaison.getAlt() != KeyCombination.ModifierValue.DOWN){
                combinaison = new KeyCharacterCombination("", combinaison.getShift(), combinaison.getControl(),
                        KeyCombination.ModifierValue.DOWN, combinaison.getMeta(), combinaison.getShortcut());
            }else if(e.isMetaDown() && combinaison.getMeta() != KeyCombination.ModifierValue.DOWN){
                combinaison = new KeyCharacterCombination("", combinaison.getShift(), combinaison.getControl(),
                        combinaison.getAlt(), KeyCombination.ModifierValue.DOWN, combinaison.getShortcut());
            }else if(e.isShortcutDown() && combinaison.getShortcut() != KeyCombination.ModifierValue.DOWN){
                combinaison = new KeyCharacterCombination("", combinaison.getShift(), combinaison.getControl(),
                        combinaison.getAlt(), combinaison.getMeta(), KeyCombination.ModifierValue.DOWN);
            }
            
            updateText();
            e.consume();
        });
        super.input.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            hasReleased = true;
            e.consume();
        });
        super.input.addEventFilter(KeyEvent.KEY_TYPED, Event::consume);
        
        if(removeShortcutOption){
            addButton(TR.tr("dialogs.keyCodeCombinaisonInput.deleteCombinaison"), ButtonPosition.OTHER_LEFT);
        }
        
        Platform.runLater(() -> {
            super.input.deselect();
            super.input.end();
        });
    }
    
    private void updateText(){
        super.input.setText(combinaison.getDisplayText() + (keyCode == null ? "" : keyCode.getChar()));
        super.input.deselect();
        super.input.end();
    }
    
    public KeyCodeCombination getKeyCodeCombinaison(){
        return new KeyCodeCombination(keyCode, combinaison.getShift(), combinaison.getControl(),
                combinaison.getAlt(), combinaison.getMeta(), combinaison.getShortcut());
    }
    public boolean isCombinaisonContainingAnyShortcutKey(){
        return combinaison.getControl() == KeyCombination.ModifierValue.DOWN || combinaison.getAlt() == KeyCombination.ModifierValue.DOWN
                || combinaison.getMeta() == KeyCombination.ModifierValue.DOWN || combinaison.getShortcut() == KeyCombination.ModifierValue.DOWN;
    }
    
    public Result showAndWaitGetResult(){
        ButtonPosition button = super.getShowAndWaitGetButtonPosition(ButtonPosition.CLOSE);
        if(button == ButtonPosition.DEFAULT){
            if(keyCode == null || (requireShortcutKey && !isCombinaisonContainingAnyShortcutKey())){
                new WrongAlert(TR.tr("dialogs.keyCodeCombinaisonInput.error.noCombinaison.header"),
                        TR.tr("dialogs.keyCodeCombinaisonInput.error.noCombinaison.details"), false).showAndWait();
                return showAndWaitGetResult();
            }
            return Result.VALIDATE;
        }else if(button == ButtonPosition.OTHER_LEFT){
            return Result.DELETE;
        }
        return Result.CANCEL;
    }
    
    
    
}
