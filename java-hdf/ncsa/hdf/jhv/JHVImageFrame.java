/****************************************************************************
 * NCSA HDF                                                                 *
 * National Comptational Science Alliance                                   *
 * University of Illinois at Urbana-Champaign                               *
 * 605 E. Springfield, Champaign IL 61820                                   *
 *                                                                          *
 * For conditions of distribution and use, see the accompanying             *
 * hdf/COPYING file.                                                        *
 *                                                                          *
 ****************************************************************************/

package ncsa.hdf.jhv;

import java.applet.Applet;
import java.net.URL;
import java.util.Hashtable;
import java.awt.image.*;

import java.lang.Thread;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import ncsa.hdf.hdflib.*;


//----------------------------------------------------------------------------
//
//  Upgraded to the JDK 1.1.1b Event Model
//   - Apu Kapadia May 21st, 1997.
//
//-----------------------------------------------------------------------------

/**
 * This class will display the image in the new frame winthin the canvas 
 * what about panel control?
 */
public class JHVImageFrame extends Frame implements ItemListener, ActionListener, AdjustmentListener{

  /** the canvas that the image will  be painted. */
  JHVImageCanvas       imageCanvas = null;

  /** zoom panel responded to JHVImageCanvas */
  ScrollingPanel       zoomImagePanel = null;

  /** the canvas that the image will be drawed 
    responding to the HDF node selected */
  JHVCanvas		jhvCanvas = null;

  /** the HDF tree browser applet */
  JHV 			applet_ = null;

  /** default frame size */
  int frameWidth	= 710;
  int frameHeight	= 420;

  /** set animated speed choice  */
  Choice speedsChoice  = new Choice();

  /** the constants of the animation operation  */
  public  final static int NEXT     = 1;
  public  final static int PREVIOUS = 2;
  public  final static int BACKWARD = 3;
  public  final static int FORWARD  = 4;

  /** the current plane number of the sequence images  */
  int		numPlane = 1;

  /** the current plane number of the sequence images  */
  /**  the total image number */
  int		plane    = 1;
    
  /** Scrollbar associated with the image panel */
  Scrollbar   hScrollbar = null;
  Scrollbar   vScrollbar = null;
    
  CheckboxGroup  zoomFactorGroup ;
  Checkbox       zoomFactor100   ;
  Checkbox       zoomFactor200   ;
  Checkbox       zoomFactor500   ;
  Checkbox       zoomFactor800   ;
  Checkbox       zoomFactor1000   ;
    
  public float   zoomFactor = 2.0f;
    
  Button spButton;

  // animated operation button
  Button nextButton;
  Button prevButton;
  Button foreButton;
  Button backButton;
  Button stopButton;

  JHVSDSImageControl	sdsImageControl;
  JHVImageRGBControl	imageRGBControl;
  JHVImageBCControl	imageBCControl;

  MenuItem		adjustMenuItem;

  /** Create the seprate frame based on the HDF browser's image canvas.
   *  This frame will display the HDF raster image with scroll panel
   * @param app the JHV applet name
   */
  public JHVImageFrame(JHVCanvas canvas_) {
    
    // JHV applet 
    applet_ = canvas_.app;

    // set HDF Browser image canvas
    jhvCanvas = canvas_;

    // set the frame tittle
    setTitle();

    // set menu bar
    setMenuBar(this.createJHVImageMenuBar());
 
    // image canvas
    imageCanvas = new JHVImageCanvas(this);
             
    // set jhv canvas associated with image canvas
    imageCanvas.jhvCanvas = canvas_;
    
    // set image
    if (canvas_.currentImage != null)
      // set image for image canvas
      imageCanvas.setImage(canvas_.currentImage);

    // set image size
    int w = canvas_.getImageWidth();
    int h = canvas_.getImageHeight();
    imageCanvas.setImageSize(w,h);
	
    // set canvas size
    // imageCanvas.setCanvasSize(w,h);
    imageCanvas.setCanvasSize(200,200);
    
    // repaint the image
    imageCanvas.repaint();
         
    // get HDF Object Node;
    imageCanvas.setHDFObjectNode( canvas_.node );
	
    // initialize the HDF based on the selected HDF node
    imageCanvas.initHDF();
    
    // set layout manager
    setLayout(new BorderLayout());
    
    // GUI
    add("Center", createDisplayItems());
    
    // set scrollbar
    imageCanvas.setScrollbar(hScrollbar, vScrollbar);
    
    //imagePanel.imageCanvas.checkSize();
    imageCanvas.checkSize();
    speedsChoice.addItemListener(this);
    this.addWindowListener(new WindowClosedProcess());
  }

  /** setup the frame tittle */
  public void setTitle() {
    String winString = " Image from HDF";
    super.setTitle(winString);
  }

    public void set_Cursor(Cursor cursor) {

	// change corsor type
	 ((Component)this).setCursor(cursor);

	// default toolkit
	Toolkit.getDefaultToolkit().sync();
    }

    /** create menubar for JHV image viewer */
    public MenuBar createJHVImageMenuBar() {
    
        // new menubar
        MenuBar  jhvImageMenuBar = new MenuBar();
        jhvImageMenuBar.add(createFileMenu("File"));
  	jhvImageMenuBar.add(createEditMenu("Edit"));
    	jhvImageMenuBar.add(createPaletteMenu("Palette"));
	jhvImageMenuBar.add(createImageMenu("Image"));
	jhvImageMenuBar.add(createFilterMenu("Filter"));
	jhvImageMenuBar.add(createContourMenu("Contour"));

        return  jhvImageMenuBar;
    }

   /** create a file menu by provoded title
    * @param menuTitle the menu title
    */
    public Menu createFileMenu(String  menuTitle) {
    
        // new menu by specified menuTitle
        Menu fileMenu = new Menu(menuTitle);
    
        // add menuItem 
    
	MenuItem CloseWindowMItem = new MenuItem("Close Window");
	CloseWindowMItem.addActionListener(this);
        fileMenu.add(CloseWindowMItem);
    
        return fileMenu;
    
    }

   /** create a edit  menu by provoded title such like bright/contrast ...
    * @param menuTitle the menu title
    */
    public Menu createEditMenu(String  menuTitle) {
    
        // new menu by specified menuTitle
        Menu editMenu = new Menu(menuTitle);
    
        // add menuItem 
	
	MenuItem UndoAllMItem = new MenuItem("Undo All");
	UndoAllMItem.addActionListener(this);
 	editMenu.add(UndoAllMItem);
	editMenu.addSeparator();
	MenuItem UndoMItem = new MenuItem("Undo");
	UndoMItem.addActionListener(this);
	editMenu.add(UndoMItem);	
        return editMenu;
    
    }

   /** create a palette menu by provoded title
    * @param menuTitle the menu title
    */
    public Menu createPaletteMenu(String  menuTitle) {
    
        // new menu by specified menuTitle
        Menu paletteMenu = new Menu(menuTitle);
    
        // add menuItem 
	MenuItem NewPaletteMItem = new MenuItem("New Palette");
	NewPaletteMItem.addActionListener(this);
	paletteMenu.add(NewPaletteMItem);
	paletteMenu.addSeparator();
	MenuItem SetPaletteMItem = new MenuItem("Set Palette");
	SetPaletteMItem.addActionListener(this);
	paletteMenu.add(SetPaletteMItem);

        return paletteMenu;
    
    }

   /** create a image process menu by provoded title such like bright/contrast ...
    * @param menuTitle the menu title
    */
    public Menu createImageMenu(String  menuTitle) {
    
        // new menu by specified menuTitle
        Menu imageMenu = new Menu(menuTitle);
      
	// add menuItem 
        adjustMenuItem = new MenuItem("Range Modification");
 	adjustMenuItem.addActionListener(this);
	imageMenu.add(adjustMenuItem);

	imageMenu.addSeparator();
	
	MenuItem RGBBalanceMItem = new MenuItem("RGB Balance");
	RGBBalanceMItem.addActionListener(this);
	imageMenu.add(RGBBalanceMItem);
	
	MenuItem BCMItem = new MenuItem("Brightness/Contrast" );
	BCMItem.addActionListener(this);
	imageMenu.add(BCMItem);
	
	MenuItem NegativeMItem = new MenuItem("Negative");
	NegativeMItem.addActionListener(this);
	imageMenu.add( NegativeMItem );
 
        return imageMenu;
    
    }

   /** create a image filter menu by provoded title 
    * @param menuTitle the menu title
    */
    public Menu createFilterMenu(String  menuTitle) {
    
        // new menu by specified menuTitle
        Menu filterMenu = new Menu(menuTitle);
    
        // add menuItem 
	
	MenuItem smooth = new MenuItem("Smooth");
	smooth.addActionListener(this);
 	filterMenu.add(smooth);

	MenuItem sharpen = new MenuItem("Sharpen");
	sharpen.addActionListener(this);
	filterMenu.add(sharpen);

	MenuItem addNoise = new MenuItem("Add Noise");
	addNoise.addActionListener(this);
	filterMenu.add(addNoise);

	MenuItem findEdge = new MenuItem("Find Edge");
	findEdge.addActionListener(this);
	filterMenu.add(findEdge);

	MenuItem flipH = new MenuItem("Flip(Horizontal)" );
	flipH.addActionListener(this);
	filterMenu.add(flipH);

	MenuItem flipV = new MenuItem("Flip(Vertical)" );
	flipV.addActionListener(this);
	filterMenu.add(flipV);

	MenuItem emboss = new MenuItem("Emboss");
	emboss.addActionListener(this);
	filterMenu.add(emboss);

	MenuItem polarize = new MenuItem("Polarize");
	polarize.addActionListener(this);
	filterMenu.add(polarize);

	MenuItem rotate = new MenuItem("Rotate");
	rotate.addActionListener(this);
	filterMenu.add(rotate);
		
        return filterMenu;
    
    }

   /** create a image contour menu by provoded title 
    * @param menuTitle the menu title
    */
    public Menu createContourMenu(String  menuTitle) {
    
        // new menu by specified menuTitle
        Menu contourMenu = new Menu(menuTitle);
    
        // add menuItem 
	MenuItem level3 = new MenuItem("Level 3");
	level3.addActionListener(this);
 	contourMenu.add(level3);

	MenuItem level5 = new MenuItem("Level 5");
	level5.addActionListener(this);
	contourMenu.add(level5);

	MenuItem level9  = new MenuItem("Level 9");
	level9.addActionListener(this);
	contourMenu.add(level9);
			
        return contourMenu;
    
    }

/** create new GUI for ImageFrame */
public Panel createDisplayItems() {
 
    // return panel
    Panel retPanel = new Panel();
     
    // image panel
    Panel imgPanel = new Panel();
    imgPanel.setLayout(new BorderLayout());
    
    // create the scrollbar
    vScrollbar = new Scrollbar();
    vScrollbar.addAdjustmentListener(this);
    hScrollbar = new Scrollbar(Scrollbar.HORIZONTAL);
    hScrollbar.addAdjustmentListener(this);

    imgPanel.add("Center", imageCanvas );
    imgPanel.add("East",   vScrollbar);
    imgPanel.add("South",  hScrollbar);
     
    // control area	  
    Panel ctrPanel = new Panel();
    ctrPanel.setLayout(new GridLayout(1,0));
    
    ctrPanel.add(spButton = new Button("Spreadsheet"));
    spButton.addActionListener(this);

    speedsChoice.add("0.0");
    speedsChoice.add("0.1");
    speedsChoice.add("0.2");
    speedsChoice.add("0.5");
    speedsChoice.add("1.0");
    speedsChoice.add("2.0");
    
    ctrPanel.add(speedsChoice);
    
    ctrPanel.add(nextButton =new Button("Next"));
    ctrPanel.add(prevButton =new Button("Previous"));
    ctrPanel.add(backButton =new Button("Backward"));
    ctrPanel.add(foreButton =new Button("Forward"));
    ctrPanel.add(stopButton =new Button("Stop"));
    Button dismissButton;
    ctrPanel.add(dismissButton =new Button("Dismiss"));    
   
    nextButton.addActionListener(this);
    prevButton.addActionListener(this);
    backButton.addActionListener(this);
    foreButton.addActionListener(this);
    stopButton.addActionListener(this);
    dismissButton.addActionListener(this);

    // zoom image panel
    zoomImagePanel = new ScrollingPanel(100,100);
     
    // control area	  
    Panel zCtrPanel = new Panel();
    zCtrPanel.setLayout(new FlowLayout());
    
    // new CheckboxGroup
    CheckboxGroup  zoomFactorGroup = new CheckboxGroup();
    Checkbox       zoomFactor100   = new Checkbox("100%", zoomFactorGroup, false);
    Checkbox       zoomFactor200   = new Checkbox("200%", zoomFactorGroup, true);
    Checkbox       zoomFactor500   = new Checkbox("500%", zoomFactorGroup, false);
    Checkbox       zoomFactor800   = new Checkbox("800%", zoomFactorGroup, false);
    Checkbox       zoomFactor1000   = new Checkbox("1000%", zoomFactorGroup, false);
    
    zoomFactor100.addItemListener(this);
    zoomFactor200.addItemListener(this);
    zoomFactor500.addItemListener(this);
    zoomFactor800.addItemListener(this);
    zoomFactor1000.addItemListener(this);
    
    zCtrPanel.add(zoomFactor100);
    zCtrPanel.add(zoomFactor200);
    zCtrPanel.add(zoomFactor500);
    zCtrPanel.add(zoomFactor800);
    zCtrPanel.add(zoomFactor1000);
       
    Panel lPanel = new Panel();
    lPanel.setLayout(new BorderLayout());
    
    lPanel.add("Center",zoomImagePanel);
    lPanel.add("South", zCtrPanel);
  
    /***********************************************************
    GridBagConstraints panelPos = new GridBagConstraints();
    // set retPanel
    GridBagLayout      pLayout = new GridBagLayout();
    retPanel.setLayout(pLayout);
    
    panelPos.fill    = GridBagConstraints.BOTH;
    
    panelPos.weighty = 2.5;
    panelPos.weightx = 1.0;
    pLayout.setConstraints (imgPanel, panelPos);
    retPanel.add (imgPanel);
    
    panelPos.gridwidth = GridBagConstraints.REMAINDER;
    panelPos.weighty   = 2.5;
    panelPos.weightx   = 1.0;
    pLayout.setConstraints (lPanel, panelPos);
    retPanel.add (lPanel);
    
    panelPos.weighty   = 0.0;
    panelPos.weightx   = 1.0;
    pLayout.setConstraints (ctrPanel, panelPos);
    retPanel.add (ctrPanel);
    *********************************************************/
    Panel uPanel = new Panel();
    uPanel.setLayout(new GridLayout(1,2));
    uPanel.add(imgPanel);
    uPanel.add(lPanel);
    
    retPanel.setLayout(new BorderLayout());
    retPanel.add("Center", uPanel);
    retPanel.add("South", ctrPanel);
    
    return retPanel;
    
}

 

  /** set the action flag for displaying the image
   * @param actionFlag the action flag value
   * @exception IOException If an I/O error has occurred.
   */ 
  public void setImage(int actionFlag) throws IOException {
    
    int   i;
    Image img_;

    // "Next" button pressed
    if (actionFlag == NEXT) {  // next...

      // next image plane number
      ++numPlane;
      if (numPlane > plane)
	numPlane = 1;
      
      //next image
      img_ = imageCanvas.getImage(numPlane);
      
      // set image for hdfCanvas and repaint it
      applet_.hdfCanvas.setImage(img_);
      
      // set image for current canvas & repaint it
      imageCanvas.setImage(img_);
      
    }
    // "Previous" button pressed
    if (actionFlag == PREVIOUS) {  // previous

      // previous image plane number
      --numPlane;
      
      // adjust the value
      if (numPlane < 1)
	numPlane = plane;

      // get previous image
      img_ = imageCanvas.getImage(numPlane);
      
      // set image for hdfCanvas
      applet_.hdfCanvas.setImage(img_);

      // set image for current canvas
      imageCanvas.setImage(img_);

    }

    // "Backward" pressed
    if (actionFlag == BACKWARD) {  // backward...

      // set action to "Backward"
      imageCanvas.setAction(BACKWARD);
	   
    }

    // "Forward" pressed
    if (actionFlag == FORWARD) {  // forward...
      // set action
      imageCanvas.setAction(FORWARD);
    }

  }

  /** popup the new frame actually */
  public void popup() {
    
    // resize the frame; (which default size is best?)
    setSize(frameWidth, frameHeight);

    //paint the canvas
    imageCanvas.repaint();
    
    // show the whole components
    show();

    // adjust the size of the frame

    setSize(frameWidth, frameHeight);
    
    // set scrollbar value
    imageCanvas.setHScrollValue();
    imageCanvas.setVScrollValue();
  }

    public void close() {
	// shut down all my control
	// for sds
	if (sdsImageControl != null) {
	   sdsImageControl.close();
	   sdsImageControl = null;
	}

	if (imageRGBControl != null) {
	   imageRGBControl.close();
	   imageRGBControl = null;
	}

	if (imageBCControl != null) {
	   imageBCControl.close();
	   imageBCControl = null;
	}
    }

    /** Enables annimated button */
    public void buttonEnable() {

	nextButton.setEnabled(true);
	prevButton.setEnabled(true);
	stopButton.setEnabled(true);
	foreButton.setEnabled(true);
	backButton.setEnabled(true);
	
    }

    /** Disables annimated button */
    public void buttonDisable() {
	
	nextButton.setEnabled(false);
	prevButton.setEnabled(false);
	stopButton.setEnabled(false);
	foreButton.setEnabled(false);
	backButton.setEnabled(false);
	
    }

    public void handleMenuEvent(String arg) {

	    spButton.setEnabled(true);

	    // set cursor type to "WAIT_CURSOR"
	    set_Cursor(new Cursor(Cursor.WAIT_CURSOR));
 
	    // Close Window
	    if ("Close Window".equals(arg)) {		
		// stop the animation
		imageCanvas.stop();
		// close the window
		dispose();
		// close all of my control
		close();
	    }

	    // Undo All , Undo ...
	    if (("Undo All".equals(arg)) || ("Undo".equals(arg)))
		handleEditMenuEvent(arg);

	    // "New palette"  , "Set Palette"
	    if (("New Palette".equals(arg)) || ("Set Palette".equals(arg)))
		handlePaletteMenuEvent(arg);

	    // Image Filter: Emboss, Smooth, Sharp, Flip ....
	    if (("Emboss".equals(arg))    || ("Smooth".equals(arg)) ||	
	        ("Sharpen".equals(arg ))    || ("Rotate".equals(arg)) ||
		("Add Noise".equals(arg)) ||("Find Edge".equals(arg)) ||
	 	("Flip(Horizontal)".equals(arg)) || 
		("Flip(Vertical)".equals(arg))   || ("Polarize".equals(arg)) )
		
		// goto HandleFilterEvent()
		handleFilterEvent(arg);

 	   // Image Filter: Just for RGB, do not change pixels value ....
	   // such as Negative, Adjust Range(redo image), Contrast...

	   if ( ("Negative".equals(arg)) ||("Range Modification".equals(arg)) ||
		("RGB Balance".equals(arg)) ||("Brightness/Contrast".equals(arg))  )
	     	handleImageMenuEvent(arg);

	    // contour
	    if (("Level 3".equals(arg))||("Level 5".equals(arg))||("Level 9".equals(arg)))
		handleContourMenuEvent(arg);
	
    	    // set cursor type to "DEFAULT_CURSOR"
	    set_Cursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    // Handle Edit menu events
    public void handleEditMenuEvent(String arg) {

	    // Undo All
	    if ("Undo All".equals(arg)) 
		imageCanvas.recoverImage();

	    // Undo ...
	    if ("Undo".equals(arg)) {
		spButton.setEnabled(false);
		jhvCanvas.setImage(imageCanvas.preImage);
		imageCanvas.setImage(imageCanvas.preImage);
	    }
    }

    public void setNewPalette() {

	imageCanvas.setNewPalette();
    }

    public void openNewPalette() {

	imageCanvas.openNewPalette();
    }

    public void handlePaletteMenuEvent(String arg) {
	
	    if ("New Palette".equals(arg)) 
		openNewPalette();
 
	    if ("Set Palette".equals(arg)) 
		setNewPalette();
    }

    public void setImageFilter(ImageFilter filter) {

	spButton.setEnabled(false);
	imageCanvas.setImageFilter(filter);
    }

    public void handleImageMenuEvent(String arg) {

  	    if ("Negative".equals(arg)) 
		setImageFilter((ImageFilter)new NegativeFilter());
	    else if ("Range Modification".equals(arg))   // image
	              sdsImageControl = new JHVSDSImageControl(imageCanvas);
 		 else if ("RGB Balance".equals(arg))  
	      		imageRGBControl = new JHVImageRGBControl(imageCanvas);
		      else if ("Brightness/Contrast".equals(arg))  
 			      imageBCControl = new JHVImageBCControl(imageCanvas);
    }

    public void handleFilterEvent(String arg) {

	    // Image Filter
	    if ("Emboss".equals(arg)) 
		setImageFilter((ImageFilter)new EmbossFilter());
     
	    if ("Smooth".equals(arg)) 
		setImageFilter((ImageFilter)new BlurFilter());
   
	    if ("Sharpen".equals(arg)) 
		setImageFilter((ImageFilter)new SharpFilter(3));
  
	    if ("Rotate".equals(arg)) 
		setImageFilter((ImageFilter)new RotateFilter((double)Math.PI/2.0d));
  
	    if ("Add Noise".equals(arg)) 
		setImageFilter((ImageFilter)new NoiseFilter());

   	    if ("Find Edge".equals(arg)) 
		setImageFilter((ImageFilter)new EdgeFilter());

  	    if ("Flip(Horizontal)".equals(arg))  // Horizontal ...
		setImageFilter((ImageFilter)new FlipFilter(FlipFilter.HORIZONTAL));
   
	    if ("Flip(Vertical)".equals(arg))  // Vertical ...
		setImageFilter((ImageFilter)new FlipFilter(FlipFilter.VERTICAL));

   	    if ("Polarize".equals(arg))  
		setImageFilter((ImageFilter)new PolarFilter());
    }

    public void handleContourMenuEvent(String arg) {

	    if ("Level 3".equals(arg)) 
		setImageFilter((ImageFilter)new ContourFilter(3));
	    if ("Level 5".equals(arg)) 
		setImageFilter((ImageFilter)new ContourFilter(5));
	    if ("Level 9".equals(arg)) 
		setImageFilter((ImageFilter)new ContourFilter(9));
  }

  /**
   * Handles Button Events
   */
  public void actionPerformed(ActionEvent e)
  {

    plane = imageCanvas.numberOfImage;
    String arg = e.getActionCommand();

    if ("Spreadsheet".equals(arg))
	 imageCanvas.makeSpreadsheet();
      
      // "stop" pressed, then kill the current thread
      if ("Stop".equals(arg)) 		
	imageCanvas.stop();
      
      // "dimiss" pressed, then close the frame
      if ("Dismiss".equals(arg)) {

	// Data Frame will be distroyed

	// close HDF file(Do we need close the HDF now?)
	// imageCanvas.hdf.close();

	// stop the animation
	imageCanvas.stop();

	// close the window
	dispose();

	// close all of my control
	close();
      }

      // "Next" pressed
      if ("Next".equals(arg)) { //next image
	try {
	  // set the next image
	  setImage(NEXT);
	} catch(Exception ex) {};
      }

      // "Previous" pressed
      if ("Previous".equals(arg)) { //previous Image
	try {
	  // set the previous image
	  setImage(PREVIOUS);
	} catch (Exception ex) {};
      }

      // "Backward" pressed
      if ("Backward".equals(arg)) { //All of images (backward)
	try {
	  // set action for image operation
	  setImage(BACKWARD);
	} catch (Exception ex) {};
      }

      // "Forward" pressed
      if ("Forward".equals(arg)) { //All of images (forward)
	try {
	  // set action for image operation
	  setImage(FORWARD);
	} catch (Exception ex) {};
      }
  
      
      //Event HAS to be a MenuItem generated event.
      handleMenuEvent(arg);
  }

  public void itemStateChanged(ItemEvent e)
  {
    plane = imageCanvas.numberOfImage;
    ItemSelectable target = e.getItemSelectable();

    if(target instanceof Choice)
      {
	Choice choice = (Choice) target;
	// speed choice;
	String speedStr = choice.getSelectedItem();
	if (Character.isDigit(speedStr.charAt(0))) 
	  {
	    // get spreed value
	    float tmpVal = Float.valueOf(speedStr).floatValue()* 1000.0f;
	    int sp = (int)(tmpVal);
	    
	    // set animationed speed
	    imageCanvas.setSpeeds(sp);
	  } 
      }

    if(target instanceof Checkbox)
      {
	Checkbox checkBox = (Checkbox)target;

	String label = checkBox.getLabel();
	
	if (label.equals("100%"))
	    zoomFactor = 1.0f;

	if (label.equals("200%"))
	    zoomFactor = 2.0f;
	if (label.equals("500%"))
	    zoomFactor = 5.0f;
	if (label.equals("800%"))
	    zoomFactor = 8.0f;
	if (label.equals("1000%"))
	    zoomFactor = 10.0f;
      }

  }
  
  /**
   * Handles the event. Returns true if the event is handled and
   * should not be passed to the parent of this component. The default
   * event handler calls some helper methods to make life easier
   * on the programmer.
   * @param evt the event
   * @see java.awt.Component#handleEvent
   */
  public void adjustmentValueChanged(AdjustmentEvent e)
  {
    int type = e.getAdjustmentType();
    Scrollbar target = (Scrollbar) e.getAdjustable();

    switch (type) { 
    case AdjustmentEvent.UNIT_INCREMENT:
    case AdjustmentEvent.UNIT_DECREMENT:
    case AdjustmentEvent.BLOCK_INCREMENT:
    case AdjustmentEvent.BLOCK_DECREMENT:
    case AdjustmentEvent.TRACK:

      // detect the vertical scrollbar
      if (target ==  vScrollbar) {
		
	// get translated value
	imageCanvas.ty = vScrollbar.getValue();
		
	// repaint the graphics
	imageCanvas.repaint();
	
      }
      // detect the horizontal scrollbar
      if (target ==  hScrollbar) {
	
	// get translated value
	imageCanvas.tx = hScrollbar.getValue();
	    
	// repaint the graphics
	imageCanvas.repaint();
      }
    }
  }

}