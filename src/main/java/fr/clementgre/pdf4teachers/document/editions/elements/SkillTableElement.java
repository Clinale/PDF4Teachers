/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.EditionSkill;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SkillTableElement extends GraphicElement{
    
    private final LongProperty assessmentId = new SimpleLongProperty();
    private final LongProperty studentId = new SimpleLongProperty();
    private final ListProperty<EditionSkill> editionSkills = new SimpleListProperty<>();
    
    
    private final SkillTableGridPane gridPane = new SkillTableGridPane(this);
    
    public SkillTableElement(int x, int y, int pageNumber, boolean hasPage, int width, int height, long assessmentId, long studentId, ArrayList<EditionSkill> editionSkills){
        super(x, y, pageNumber, width, height, RepeatMode.STRETCH, ResizeMode.CORNERS);
        this.assessmentId.set(assessmentId);
        this.studentId.set(studentId);
        this.editionSkills.set(FXCollections.observableList(editionSkills));
        
        if(hasPage && getPage() != null) setupPage();
    }
    
    @Override
    public void initializePage(int pageNumber, double x, double y){
        this.pageNumber = pageNumber;
        setupPage();
        checkLocation(x, y, false);
    }
    
    private void setupPage(){
        
        editionSkills.addListener((observable, oldValue, newValue) -> updateLayout());
        assessmentId.addListener((observable, oldValue, newValue) -> updateLayout());
    
        setupGeneral(gridPane);
        
        if(getRealWidth() == 0 && getRealHeight() == 0){
            checkLocation(getPage().fromGridX(22700), getPage().fromGridX(22700), getPage().fromGridX(120000), 0, false);
        }
    
        // Update the gridPane scale
        widthProperty().addListener((observable) -> updateGridPaneScale());
        heightProperty().addListener((observable) -> updateGridPaneScale());
    
        
    
        // Update element height when gridPane height changes. (Adding new skill or new row)
        gridPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            
            double newHeight;
            if(getRealHeight() == 0){ // Height undefined => Default height
                newHeight = newValue.doubleValue() * DEFAULT_SCALE;
            }else{
                if(newValue.doubleValue() == 0){
                    // Grid is now empty => Set the element's height to 0 so it can then be auto defined.
                    newHeight = 0;
                }else if(getHeight() == 0 || oldValue.doubleValue() == 0 || Double.isNaN(getRatio()) || Double.isInfinite(getRatio())){
                    // Values are not defined => keep the current height.
                    return;
                }else{
                    // Grow the height of the element as the gridPane height grows
                    newHeight = getHeight() * newValue.doubleValue() / oldValue.doubleValue();
                }
            }
    
            checkLocation(getLayoutX(), getLayoutY(), getWidth(), newHeight, false);
            getPage().layout(); // Required to update the visual bounds of the element
            updateGridPaneScale();
        });
        
        Platform.runLater(this::updateGridPaneScale);
        
    }
    private final double DEFAULT_SCALE = .8;
    private final double MAX_SCALE = 1.4;
    private final double MIN_SCALE = 0.4;
    private void updateGridPaneScale(){
        double scale = StringUtils.clamp(getHeight() / gridPane.getHeight(), MIN_SCALE, MAX_SCALE);
        // Using scale transform to make sure the pivot point is always (0, 0)
        gridPane.getTransforms().setAll(new Scale(scale, scale));
        // Grid pane width always match the element width, considering the scaling.
        gridPane.setPrefWidth(getWidth() / scale);
        gridPane.setClip(new Rectangle(getWidth()/scale, getHeight()/scale));
    
        getPage().layout(); // Required to update the visual bounds of the element
    }
    
    @Override
    public void checkLocation(double itemX, double itemY, double width, double height, boolean allowSwitchPage){
        boolean itemYChanged = itemY != getLayoutY();
        // If the Y position changed, it means that the element is resized via N/W grab cursor so the origin needs to be updated as well.
        super.checkLocation(itemX, itemY, width, height, allowSwitchPage);
        
        // HEIGHT - min and max scale
        if(getWidth() == 0 || getHeight() == 0 || Double.isNaN(getRatio()) || Double.isInfinite(getRatio())) return;
        double scale = getPage().fromGridY(getRealHeight()) / gridPane.getHeight(); // Not clamped
        double clampedScale = StringUtils.clamp(scale, MIN_SCALE, MAX_SCALE);
        if(scale != clampedScale){
            int newRealHeight = getPage().toGridY(gridPane.getHeight() * clampedScale);
            if(getRealHeight() != newRealHeight){
                if(itemYChanged) setRealY(getRealY() + getRealHeight() - newRealHeight); // When resized from north or west
                setRealHeight(newRealHeight);
            }
        }
    }
    
    public void updateLayout(){
        gridPane.updateLayout();
    }
    
    @Override
    public void addedToDocument(boolean markAsUnsave){
        MainWindow.skillsTab.registerSkillTableElement(this);
        updateLayout();
    }
    @Override
    public void removedFromDocument(boolean markAsUnsave){
        super.removedFromDocument(markAsUnsave);
    }
    @Override
    public void delete(boolean markAsUnsave, UType undoType){
        // SkillTableElement can't be deleted and should always be kept.
    }
    @Override
    public LinkedHashMap<Object, Object> getYAMLData(){
        LinkedHashMap<Object, Object> data = super.getYAMLPartialData();
        data.put("page", pageNumber);
        data.put("assessmentId", assessmentId.get());
        data.put("studentId", studentId.get());
        // Useless to save editionSkills that have no matching Notation (id == 0)
        data.put("list", editionSkills.get().stream().filter(s -> s.getNotationId() != 0).map(EditionSkill::toYAML).toList());
        return data;
    }
    
    public static void readYAMLDataAndCreate(HashMap<String, Object> data){
        SkillTableElement element = readYAMLDataAndGive(data, true);
        if(MainWindow.mainScreen.document.getPagesNumber() > element.getPageNumber())
            MainWindow.mainScreen.document.getPage(element.getPageNumber()).addElement(element, false, UType.NO_UNDO);
    }
    
    public static SkillTableElement readYAMLDataAndGive(HashMap<String, Object> data, boolean hasPage){
        int x = (int) Config.getLong(data, "x");
        int y = (int) Config.getLong(data, "y");
        int width = (int) Config.getLong(data, "width");
        int height = (int) Config.getLong(data, "height");
        
        int page = (int)  Config.getLong(data, "page");
        
        long assessmentId = Config.getLong(data, "assessmentId");
        long studentId = Config.getLong(data, "studentId");
    
        ArrayList<EditionSkill> skills = new ArrayList<>();
        for(Object skillData : Config.getList(data, "list")){
            if(skillData instanceof Map) skills.add(EditionSkill.getFromYAML((HashMap<String, Object>) skillData));
        }
        
        return new SkillTableElement(x, y, page, hasPage, width, height, assessmentId, studentId, skills);
    }
    
    
    
    @Override
    public String getElementName(boolean plural){
        return null;
    }
    
    @Override
    public Element clone(){
        throw new RuntimeException("SkillTableElement can't be cloned.");
    }
    
    @Override
    protected void onMouseRelease(){
        super.onMouseRelease();
    }
    @Override
    public void onDoubleClick(){
    
    }
    
    @Override
    public void defineSizeAuto(){
    }
    
    @Override
    public void incrementUsesAndLastUse(){
    }
    
    @Override
    public double getRatio(){
        return gridPane.getWidth() / gridPane.getHeight();
    }
    
    public long getAssessmentId(){
        return assessmentId.get();
    }
    public LongProperty assessmentIdProperty(){
        return assessmentId;
    }
    public void setAssessmentId(long assessmentId){
        this.assessmentId.set(assessmentId);
    }
    public long getStudentId(){
        return studentId.get();
    }
    public LongProperty studentIdProperty(){
        return studentId;
    }
    public void setStudentId(long studentId){
        this.studentId.set(studentId);
    }
    public ObservableList<EditionSkill> getEditionSkills(){
        return editionSkills.get();
    }
    public ListProperty<EditionSkill> editionSkillsProperty(){
        return editionSkills;
    }
    public void setEditionSkills(ObservableList<EditionSkill> editionSkills){
        this.editionSkills.set(editionSkills);
    }
}
