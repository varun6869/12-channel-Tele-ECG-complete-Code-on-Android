package com.ecil.bluetooth;

import java.io.BufferedOutputStream;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

@SuppressLint("ViewConstructor") public class CreateReport extends View
{
		PaintView pv;
		Paint 			paint			= new Paint();
	    int 			width 			=5*1080;//5400
	    int 			height 			=5*770;//3850
	    int 			sample_data[]	= new int[200];
	    Bitmap 			bitmap;
	    Canvas 			can;
	    String 			lead_name[]		={"I","II","III","aVR","aVL","aVF","V1","V2","V3","V4","V5","V6"};
	    //int 			lead_arrange[]	={1,9,7,4,2,10,3,6,8,11,5,0};
	   int lead_arrange[]={1,2,8,9,10,11,7,3,5,4,6,0};
		public String 	comment;
	    String 			report_name;
	    public static String sFileTitle_png,sFileTitle_data;
	    String OWNER_PASS="ecg";
		static String USER_PASS;
		Context p_context;
		public static String sdf;
		public static boolean outofmemory_error=false,png_created=false;
		PdfContentByte cnvs;
		String Report_title;
		public File exportedPdfFile;
		
	@SuppressLint("SimpleDateFormat") public CreateReport(String comment, String password, Context context)
	{
		super(context); 
		p_context=context;
		this.comment=comment;
		USER_PASS=password;
		
		//used when ACQ of same patient but at different time 
		if(MainActivity.acq_repeat==true)
		{
			Date now=new Date();
			sdf = new SimpleDateFormat("yyyy_MM_dd HH-mm").format(now);
			PaintView.Pass_On_date=sdf;
		}
		
		if(MainActivity.iScreenWidth> PaintView.XAXIS_WD)
		{
			
			this.setDrawingCacheEnabled(true);
	        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	        can = new Canvas(bitmap);
	        draw_grid();
		}
		else
		{
				create_pdf();
				create_binary();
		}
			
        
	}
	
	//draws the grid lines required to plot data
	 public void draw_grid()
	 {
		// int end_pt_vertical=1070,end_pt_hrznt=650,dirtn_v=1,dirtn_h=2;
		paint.setColor(Color.WHITE);
		can.drawRect(0, 0,bitmap.getWidth(), bitmap.getHeight(), paint);    
		
		Draw_Lines(5*1070,1);//vertical
		Draw_Lines(5*650,2);//horizontal
		Draw_text();
		paint.setStyle(Style.FILL);
		plot_data();
		
	  }

	private void Draw_Lines(int end_pt, int dirtn) {
		try 
		{
			int x1=0,x2=0,y1=0,y2=0;
			
				if(dirtn==1)
				{
					y1=5*126;  y2=5*((650+124)-8);
				}
				else
				{
					x1=5*5;x2=5*1065;
				}
				
				for (int i = 25,j=0,k=0; i<=end_pt-5*4; i+=5*4,j++) //4 is the pixel on canvas
				{
					if(dirtn==1)//vertical
					{
						if(j%5==0)//dark pink line
						{
							paint.setColor(Color.rgb(255,51,153)); 
							paint.setStrokeWidth(2f);
							
						}
						else
						{
							 paint.setColor(Color.rgb(255, 185, 220));
							 paint.setStrokeWidth(2f);
						}
							
						can.drawLine(i, y1, i, y2, paint);
						
						//plots blue lines 
						if(j%5==0)
						{
							if((k-1)%13 == 0 || k==0)
							{
								paint.setColor(Color.BLUE); 
								paint.setStrokeWidth(5f);
								 if((k)==14||k==27||k==40)
									 can.drawLine(i, y1, i, 5*605, paint);
								 else 
									 can.drawLine(i, y1, i, y2, paint);
							}
							k++;
						}
						
					}
					else//horizontal
					{
						if(j%5==0)//dark line
						{
								paint.setColor(Color.rgb(255,51,153)); 
								paint.setStrokeWidth(2f);
								
								if((k)%24==0 || k%32==0)//draws horztal blue lines
								{
									 paint.setColor(Color.BLUE); 
									 paint.setStrokeWidth(5f);
								}
								k++;
						}
						else
						{
							 paint.setColor(Color.rgb(255, 185, 220));
							 paint.setStrokeWidth(2f);
						}
						can.drawLine(x1, (i+5*120), x2, (i+5*120), paint);
					}
				
				}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//writes the text of patient details on the ECG Report
	 private void Draw_text()
	 {
		 int p=0;
		 int x,y;
		 x=5*30;
		 
		 paint.setStyle(Style.STROKE);
		 paint.setColor(Color.RED);
		 can.drawRect(25,25, 5*1065, 5*120, paint);
		 paint.setColor(Color.BLACK);	
		 paint.setStyle(Style.FILL);
		 paint.setTextSize(50f);
		 paint.setStrokeWidth(1f);
		 
		 //prints the labels on report
		 can.drawText("Patient  ", 5*10, 5*20, paint);
		 can.drawText("CHSSNO ",5*10, 5*35, paint);
		 can.drawText("Age  ",5*10, 5*50, paint);
		 can.drawText("Sex", 5*10, 5*65, paint);
		 can.drawText("Medications",5*10, 5*80, paint);
		 can.drawText("Blood Pressure",5*10, 5*95, paint);
		 can.drawText("X=25mm/sec Y=10mm/mV  "+ PaintView.txt_filter, 5*10, 5*115, paint);
		//if value of report_sequential is 1  then it plots sequential report else plots simultaneous report
		 Report_title="(3X4 ";
		 if(MainActivity.report_sequential==1)
		 {
			 Report_title+="Sequential ECG Report)";
			 report_name="Sequential";
		 }
		 else if(MainActivity.report_sequential==0)
		 {
			 Report_title+="Simultaneous ECG Report)";
			 report_name="Simultaneous";
		 }
		 can.drawText(Report_title, 5*410, 5*115, paint);
		 can.drawText("Date: "+ PaintView.Pass_On_date, 5*300,5*20, paint);
		 can.drawText("Date of birth:"+ PaintView.Pass_On_dob, 5*300, 5*35, paint);
		 can.drawText("Height:"+ PaintView.Pass_On_ht, 5*300, 5*50, paint);
		 can.drawText("Weight:"+ PaintView.Pass_On_wt, 5*300, 5*65, paint);
		 can.drawText("Comment: ", 5*610,5*20, paint);
		
		 
		 //if the length of comment is 15 then it will directly display. 
		 String copyComment="";
		int length=(int)( paint.measureText(comment));
		int iRoundOff=(int) Math.round((length/2003.0)+0.5);//2003 no of characters set in one line in pixels in given space
		int istart=0,iend=84,y_co_ordinate=99;
		copyComment=comment; 
		
		if(!copyComment.equals(""))
		{
			if(length<=2003)
			{
				//24 is the pixel to display characters  
				can.drawText(copyComment.substring(istart,((length/24))), 3294, y_co_ordinate, paint);
				can.drawText(copyComment.substring(istart), 3294, (y_co_ordinate), paint);
			}
			else
			{
				for(int i=0;i<=(iRoundOff);i++)
				{
					if(i!=(iRoundOff-1))
						 can.drawText(copyComment.substring(istart,iend), 3294, y_co_ordinate, paint);
					else
						 can.drawText(copyComment.substring(istart), 3294, (y_co_ordinate), paint);
						
					istart=iend;
					
					if(((length)-(iend*24))<2003 && i!=(iRoundOff))
					{					
						iend=(length/24);
					}	
					else if(istart==iend)
					{	
						iend+=2*47;
					}
					y_co_ordinate+=74;
					
				 }//for(int i=0;i<=(iRoundOff);i++)
			 }//else of if(length<=376)
		}

		 //prints data on report
		 can.drawText(": "+ PaintView.Pass_On_name, 5*120,5*20, paint);
		 can.drawText(": "+ PaintView.Pass_On_chno,5*120, 5*35, paint);
		 can.drawText(": "+ PaintView.Pass_On_age,5*120,5*50, paint);
		 can.drawText(": "+ PaintView.Pass_On_gen, 5*120,5*65, paint);
		 can.drawText(": "+ PaintView.Pass_On_medi,5*120,5*80, paint);
		 can.drawText(": "+ PaintView.Pass_On_BP,5*120,5*95, paint);
		 
		 paint.setStrokeWidth(0f);
		 paint.setColor(Color.BLUE);	
		 paint.setStyle(Style.FILL);
		 for(int i=0;i<4;i++)
		 { 
			 y=5*280;
			 for(int j=0;j<3;j++)
			 {
			 	can.drawText(lead_name[p], x, y, paint);
		        p++;
		        y=y+5*160;
			 }
			 x=x+5*260; 
		 }
		 can.drawText("II", 5*30, 5*760, paint);	
	 }
	/* 
	//drawing dark lines on graph
	 private void draw_Dark_lines(int endpt,int k1)
	 {
		 paint.setColor(Color.rgb(255,51,153)); 
		 int limit=0;
	     for (int g =5, count=0; g <=endpt-4; g +=4,count++)
	     {
	     	if( count%5==0)
	     	{ //vertical dark lines
	     		if(k1==1)
	     		{
	     			//paint.setColor(Color.BLACK); 
					if(count/5==0||count/5==1|| count /5==14|| count /5==27 ||  count /5==40|| count/5==53  )
					{
						 paint.setColor(Color.BLUE); 
						 paint.setStrokeWidth(0);
						 paint.setStyle(Style.FILL);
					 }else
					 {
						 paint.setColor(Color.rgb(255,51,153)); 
						 paint.setStrokeWidth(0);
					 }
					if(count /5==14|| count /5==27 ||  count /5==40)
						limit=605;
					else
						limit=(650+124)-8;
					
					can.drawLine(g, 126, g,limit,paint);
					
					paint.setColor(Color.rgb(255,51,153)); 
					paint.setStrokeWidth(0);
					can.drawLine(285, 605, 285,(650+124)-8,paint);
					can.drawLine(545, 605, 545,(650+124)-8,paint);
					can.drawLine(805, 605, 805,(650+124)-8,paint);
					
	     		}
	     		else if(k1==2)
	     		{
	     			//horizontal dark lines 
	   			  if(count/5!=32)
	   			  {
					  paint.setColor(Color.rgb(255,51,153)); 
	   			  }
	   			  if((count/5)%24==0 || (count/5)%32==0)
	   			  {
					paint.setColor(Color.BLUE);
					paint.setStyle(Style.FILL);
	   			  }
	     		 can.drawLine(5, g+120, 1065, g+120,paint);
	     		}
	     	}
	     }
	 }*/
	
	 //plots the ECG Data on the graph 
	 public void plot_data()
	 {
		float prev_x1,xbase1,current_x1,current_y = 0, ybase,prev_y1 ;
		int x_offset[]={5*25,5*285,5*545,5*805};
		int y_offset[]={5*205,5*365,5*525};
		
		 //plot calibration on report
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(3f); 
		plot_CAL();			 
		
		for(int r = 0; r < 3; r++) 
		{
			for(int c = 0; c < 4; c++)
			{
				prev_x1=x_offset[c];
				prev_y1=y_offset[r];
				for(int d = 0; d < PaintView.XAXIS_WD; d++)
				{
					//current_y=y_offset[r]-Adjust_y_pixel(paintView.disp_report[lead_arrange[r+c*3]][d]);
					switch (c) {
					case 0:
						current_y=y_offset[r]-Adjust_y_pixel(PaintView.disp_report[lead_arrange[r+c*3]][d]);
						break;
					case 1:
						current_y=y_offset[r]-Adjust_y_pixel(PaintView.disp_report[lead_arrange[r+c*3]][d+(1300*MainActivity.report_sequential)]);
						break;
					case 2:
						current_y=y_offset[r]-Adjust_y_pixel(PaintView.disp_report[lead_arrange[r+c*3]][d+(2600*MainActivity.report_sequential)]);
						
						break;
					case 3:
						current_y=y_offset[r]-Adjust_y_pixel(PaintView.disp_report[lead_arrange[r+c*3]][d+(3900*MainActivity.report_sequential)]);

						break;
					default:
						break;
					}
					can.drawLine(prev_x1, prev_y1, x_offset[c]+d,current_y, paint);
					prev_x1=x_offset[c]+d;
					prev_y1=current_y;
				}
			}
		}
		
		//plot full lead II data 
		 prev_x1=xbase1 = 5*25;
		 prev_y1 =ybase=5*685;
		 paint.setColor(Color.BLACK);
		 paint.setStrokeWidth(3f); 
		for(int k = 0; k< PaintView.total_samples; k++)//XAXIS_WD=1300
		{
			current_x1=(float) (xbase1+(k));
			//System.out.println("data=="+paintView.disp_report[2][k]);

			current_y=(float) (ybase-Adjust_y_pixel(PaintView.disp_report[2][k]));
			can.drawLine(prev_x1, prev_y1, current_x1, current_y, paint);
			prev_x1=current_x1;
			prev_y1=current_y;
		}
		

		 create_binary();//creates binary file
		 create_pdf();//creates PDF of ECG Report
	 }
	 
	

	//plot calibration on report
	private void plot_CAL() 
	{
		 int inc=5*205;
		 
		 for(int i=0;i<4;i++)
		 {
			
			 can.drawLine(25, inc, 45, inc, paint);
			 can.drawLine(45, inc, 45, inc-200, paint);
			 can.drawLine(45, inc-200, 105, inc-200, paint);
			 can.drawLine(105, inc-200, 105, inc, paint);
			 can.drawLine(105, inc, 125, inc, paint);
			 inc+=5*160;
		 }
	}
	 
	
	//calculation for plotting data 
	private float Adjust_y_pixel(float disp_report)
	{
			//40pixel correspondsto 2 boxes, which corresonds to 1mV means 27 
		return (float) ((200/27.0)*disp_report);//disp_report is the raw data value
	}

	//function for creating binary file
	private void create_binary() 
	{
		try
		    {
				sFileTitle_data= PaintView.Pass_On_date.replaceAll(" ","_")+"_"+ PaintView.Pass_On_name.replaceAll(" ", "_");

        		File root = new File(Environment.getExternalStorageDirectory()+"/TELE-ECG/TELE-ECG Reports", PaintView.Pass_On_name);
        		
		        if (!root.isDirectory()) 
		        {
		            root.mkdirs();
		        }
		        
		        File ECG_FILE_NAME = new File(root,sFileTitle_data+".dat");
		        if(ECG_FILE_NAME.exists()){
					ECG_FILE_NAME.delete();
				}

		        RandomAccessFile raf=new RandomAccessFile(ECG_FILE_NAME,"rw");



		        raf.writeUTF("12ECG_2014\n");
		        raf.writeUTF(PaintView.Pass_On_date+"\n");
		        raf.writeUTF(PaintView.Pass_On_chno+"\n");
		        raf.writeUTF(PaintView.Pass_On_name+"\n");
		        raf.writeUTF(PaintView.Pass_On_dob+"\n");
		        raf.writeUTF(PaintView.Pass_On_age+"\n");
		        raf.writeUTF(PaintView.Pass_On_gen+"\n");
		        raf.writeUTF(PaintView.Pass_On_ht+"\n");
		        raf.writeUTF(PaintView.Pass_On_wt+"\n");
		        raf.writeUTF(PaintView.Pass_On_medi+"\n");
		        raf.writeUTF(PaintView.Pass_On_BP+"\n");
		        raf.writeUTF(PaintView.Pass_On_comment+"\n");
				raf.writeInt(MainActivity.iGain);
				raf.writeInt(PaintView.filter_state);
				raf.seek(1023);
				//writing raw data to binary file
				for(int j=0;j<12;j++)
				{
					for(int k = 0; k< PaintView.total_samples; k++)
					{
						raf.writeInt(PaintView.Gen_report[j][k]);
					}
				}
		        
		        raf.close();
		        
			//  Toast.makeText(getContext(), "Data Saved.", Toast.LENGTH_SHORT).show();
			  MainActivity.report_gen=true;
			  //MainActivity.view_report.setVisible(true);
		    }
		    catch(Exception e)
		    {
		         e.printStackTrace();
		         System.out.println("File not found exception: " +e.getMessage());
		    }
	}
	
	 private void create_png() {

		 try {
			 
				sFileTitle_png= PaintView.Pass_On_date.replaceAll(" ","_")+"_"+ PaintView.Pass_On_name.replaceAll(" ", "_")+"_"+report_name;

				File root = new File(Environment.getExternalStorageDirectory()+"/TELE-ECG/TELE-ECG Reports", PaintView.Pass_On_name);
		        if (!root.isDirectory()) 
		            root.mkdirs();
			   
		        //for .png file
		        File file1 = new File(root,sFileTitle_png+".PNG");
				file1.createNewFile();
				FileOutputStream fos1;
				fos1 = new FileOutputStream(file1);
				bitmap.compress(CompressFormat.PNG,95,fos1);
		        fos1.flush();
		        fos1.close();
			//	Toast.makeText(getContext(), "Report generated successfully..",Toast.LENGTH_SHORT).show();
				//clear bitmap and canvas for further use...
				bitmap.recycle();
				can.drawColor(Color.WHITE, Mode.CLEAR);
				png_created=true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		 catch (OutOfMemoryError ex)
		 {
			 System.out.println(ex.getMessage());
		 }
	}

	//function for creating PDF file
	private void create_pdf()
	{
		try
		{
			//if value of report_sequential is 1  then it plots sequential report else plots simultaneous report
			 Report_title="(3X4 ";
			 if(MainActivity.report_sequential==1)
			 {
				 Report_title+="Sequential ECG Report)";
				 report_name="Sequential";
			 }
			 else if(MainActivity.report_sequential==0)
			 {
				 Report_title+="Simultaneous ECG Report)";
				 report_name="Simultaneous";
			 }
			
			sFileTitle_png= PaintView.Pass_On_date.replaceAll(" ","_")+"_"+ PaintView.Pass_On_name.replaceAll(" ", "_")+"_"+report_name;
			System.out.println("sFileTitle_png="+sFileTitle_png);
			File root = new File(Environment.getExternalStorageDirectory()+"/TELE-ECG/TELE-ECG Reports", PaintView.Pass_On_name);
	        if (!root.isDirectory()) 
	            root.mkdirs();
	        
	        ///pdf generation
			File file = new File(root,sFileTitle_png+".pdf");
			file.createNewFile();
			exportedPdfFile =file;
			FileOutputStream fos;
			fos = new FileOutputStream(file);
			
			//Document document=new Document(PageSize.A4.rotate());
			// step 1
	        Document document = new Document(new Rectangle(width,height));
	        PdfWriter writer=null;
	        
			try {
				  writer = PdfWriter.getInstance(document,fos);
				writer.setEncryption(USER_PASS.getBytes(), OWNER_PASS.getBytes(),
				        PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_128);
				
				document.setMargins(0, 0, 0, 0);
				document.open();
			} catch (DocumentException e) {
				e.printStackTrace();
			}
	        PdfContentByte canvas = writer.getDirectContent();
	        
		    canvas.setColorStroke(new BaseColor(21,64,234));//blue
	        Rectangle data_rect = new Rectangle(36, 36, width-63, 3235);
	        data_rect.setBorder(Rectangle.BOX);
	        data_rect.setBorderWidth(2);
	        canvas.rectangle(data_rect);
	        canvas.stroke();
	        print_details_pdf(canvas);//prints patient details on report
	        draw_grid_pdf(canvas,1,width-63);//5350 vertical
	        draw_grid_pdf(canvas,2,3250);//3250 horizontal
	        plot_CAL_pdf(canvas);//plot calibration on report
	        plot_data_for_pdf(canvas);//plot data
			document.close();
			fos.flush();
			fos.close();
			
			if(MainActivity.report_gen==true)
			{
				MainActivity.report_gen=false;
			}
		//	Toast.makeText(getContext(), "Report in PDF generated successfully.",Toast.LENGTH_SHORT).show();
		}
		catch (IOException e)
		{
			exportedPdfFile= null;
			e.printStackTrace();
		}
	}
	
	
	
	private void plot_CAL_pdf(PdfContentByte canvas) {
		try {
			
			  int inc=(5*47)+200;//1025
				
				canvas.setColorStroke(new GrayColor(0.2f));
		        canvas.setColorFill(new BaseColor(0,0,0));
		        
				for(int i=0;i<4;i++)
				{
					canvas.moveTo(36,inc);
					canvas.lineTo(55,inc);
					canvas.moveTo(55,inc);
					canvas.lineTo(55,inc+200);
					canvas.moveTo(55,inc+200);
					canvas.lineTo(115,inc+200);
					canvas.moveTo(115,inc+200);
					canvas.lineTo(115,inc);
					canvas.moveTo(115, inc);
					canvas.lineTo(135,inc);
					canvas.fillStroke();
					inc+=(5*160);
				}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void print_details_pdf(PdfContentByte canvas) {
		try {
			 	int h=(height-(5*740));
			 	int p=0;
			 	int x,y;
				int istart=0,iend=80,y_co_ordinate=height-99;
				int NO_OF_CHAR=80,SIZE_PER_CHARACTER=24;
				
				//draws the rectangle to print details
				Rectangle patient_detail_rect = new Rectangle(36, 3250, width-63, height-36);
		        patient_detail_rect .setBorder(Rectangle.BOX);
		        patient_detail_rect .setBorderWidth(2);
		        canvas.rectangle(patient_detail_rect );
		        
		        //calculating font size
		        String text="Test";//string used for calculating the text size for rectangle
		        // try to get max font size that fit in rectangle
		        BaseFont base_font = null;
				try {
					base_font = BaseFont.createFont();
				} catch (DocumentException e) {
					e.printStackTrace();
				}
		        int textHeightInGlyphSpace = base_font.getAscent(text) - base_font.getDescent(text);
		        float fontSize = 75f * patient_detail_rect .getHeight()/textHeightInGlyphSpace;
				 
		        //phrase used to set the fontsize for the given text 
		        Phrase name = new Phrase("Patient Name     :", new Font(base_font, fontSize));
		        Phrase chno = new Phrase("Patient ID           :", new Font(base_font, fontSize));
		        Phrase age = new Phrase("Age                    :", new Font(base_font, fontSize));
		        Phrase gender = new Phrase("Sex                    :", new Font(base_font, fontSize));
		        Phrase medication = new Phrase("Medications       :", new Font(base_font, fontSize));
		        Phrase BP = new Phrase("Blood pressure  :", new Font(base_font, fontSize));
		        Phrase filter = new Phrase("X=25mm/sec Y=10mm/mV  "+ PaintView.txt_filter, new Font(base_font, fontSize));
		        Phrase title = new Phrase(Report_title, new Font(base_font, fontSize));
		        Phrase txt_comment = new Phrase("Comments: ", new Font(base_font, fontSize));
		        
		        Phrase p_name = new Phrase(PaintView.Pass_On_name, new Font(base_font, fontSize));
		        Phrase p_chno = new Phrase(PaintView.Pass_On_chno, new Font(base_font, fontSize));
		        Phrase p_age = new Phrase(PaintView.Pass_On_age, new Font(base_font, fontSize));
		        Phrase p_gen = new Phrase(PaintView.Pass_On_gen, new Font(base_font, fontSize));
		        Phrase p_medi = new Phrase(PaintView.Pass_On_medi, new Font(base_font, fontSize));
		        Phrase p_bp = new Phrase(PaintView.Pass_On_BP, new Font(base_font, fontSize));
		        
		        Phrase date = new Phrase("Date & Time  	   :", new Font(base_font, fontSize));
		        Phrase DOB = new Phrase("Date of birth     :", new Font(base_font, fontSize));
		        Phrase height = new Phrase("Height              :", new Font(base_font, fontSize));
		        Phrase weight = new Phrase("Weight             :", new Font(base_font, fontSize));
		        Phrase p_date = new Phrase(PaintView.Pass_On_date, new Font(base_font, fontSize));
		        Phrase p_DOB = new Phrase(PaintView.Pass_On_dob, new Font(base_font, fontSize));
		        Phrase p_height = new Phrase(PaintView.Pass_On_ht, new Font(base_font, fontSize));
		        Phrase p_weight = new Phrase(PaintView.Pass_On_wt, new Font(base_font, fontSize));
		        
		        //print the patient details
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,name,45,3745,0);
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,p_name,500,3750-5,0);
		        
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,chno,45,3670,0);
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,p_chno,500,3670,0);
		        
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,age,45,3595,0);
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,p_age,500,3595,0);
		        
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,gender,45,3520,0);
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,p_gen,500,3520,0);
		        
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,medication,45,3445,0);
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,p_medi,500,3445,0);
		        
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,BP,45,3370,0);
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,p_bp,500,3370,0);
		        
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,filter,45,3280,0);
		        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,title,2800,3280,0);
		        
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,date,1500,3745,0);
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,p_date,1920,3745,0);
		        
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,DOB,1500,3670,0);
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,p_DOB,1920,3670,0);
		        
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,height,1500,3595,0);
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,p_height,1920,3595,0);
		        
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,weight,1500,3520,0);
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,p_weight,1920,3520,0);
		        
		        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,txt_comment,2990,3750,0);
		        
		       
				 //print comments on report
				 //if the length of comment is 15 then it will directly display. 
				 String copyComment="";
				//int length=(int)( paint.measureText(comment));
				 int length=comment.length();
				int iRoundOff=(int) Math.round((length/NO_OF_CHAR)+0.5);//2003 no of characters set in one line in pixels in given space
				copyComment=comment; 
				
				if(!copyComment.equals(""))
				{
					if(length<=NO_OF_CHAR)
					{
						//24 is the pixel to display one character  
						Phrase comment = new Phrase(copyComment.substring(istart,((length/SIZE_PER_CHARACTER))), new Font(base_font, fontSize));
					    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,comment,3296,y_co_ordinate,0);
					    
					    Phrase comment1 = new Phrase(copyComment.substring(istart), new Font(base_font, fontSize));
					    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,comment1,3296,y_co_ordinate,0);
					}
					else
					{
						for(int i=0;i<=(iRoundOff);i++)
						{
							
							if(i!=(iRoundOff-1))
							{
								Phrase comment = new Phrase(copyComment.substring(istart,iend), new Font(base_font, fontSize));
							    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,comment,3296,y_co_ordinate,0);
							}
							else
							{
								Phrase comment = new Phrase(copyComment.substring(istart), new Font(base_font, fontSize));
							    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,comment,3296,y_co_ordinate,0);
							}
							istart=iend;
							
							if(((length)-(iend*SIZE_PER_CHARACTER))<NO_OF_CHAR && i!=(iRoundOff))
							{					
								iend=(length/SIZE_PER_CHARACTER);
							}	
							else if(istart==iend)
							{	
								iend+=2*47;
							}
							y_co_ordinate-=74;
							
						 }//for(int i=0;i<=(iRoundOff);i++)
					 }//else of if(length<=376)
				}
				
				//print lead names
				x=5*40;
				canvas.setColorFill(BaseColor.BLUE);//sets font color
				
				for(int i=0;i<4;i++)
				{ 
					y=((2600));//5*280, 
					for(int j=0;j<3;j++)
					{
						Phrase leadnames = new Phrase(lead_name[p], new Font(base_font, fontSize));
					    ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,leadnames,x,y,0);
				        p++;
				        y=y-(5*160);//800
					 }
					 x=x+5*260; //1350
				}
				
			    Phrase lead2 = new Phrase("II", new Font(base_font, fontSize));
				ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,lead2,5*40,h,0);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void plot_data_for_pdf(PdfContentByte canvas) {
		try {
			float prev_x1,xbase1,current_x1,current_y = 0, ybase,prev_y1;
			int x_offset[]={135,1435,2735,4035};
			int y_offset[]={height-2837,height-2037,height-1234};
	        canvas.setColorStroke(new GrayColor(0.2f));
	       
			for(int r = 0; r < 3; r++) 
			{
				for(int c = 0; c < 4; c++)
				{
					prev_x1=x_offset[c];
					prev_y1=y_offset[r];
					for(int d = 0; d < PaintView.XAXIS_WD; d++)
					{
						switch (c) {
						case 0:
							current_y=y_offset[r]-Adjust_y_pixel(PaintView.disp_report[lead_arrange[r+c*3]][d]);
							current_y=height-current_y;
							break;
						case 1:
							current_y=y_offset[r]-Adjust_y_pixel(PaintView.disp_report[lead_arrange[r+c*3]][d+(1300*MainActivity.report_sequential)]);
							current_y=height-current_y;
							break;
						case 2:
							current_y=y_offset[r]-Adjust_y_pixel(PaintView.disp_report[lead_arrange[r+c*3]][d+(2600*MainActivity.report_sequential)]);
							current_y=height-current_y;
							break;
						case 3:
							current_y=y_offset[r]-Adjust_y_pixel(PaintView.disp_report[lead_arrange[r+c*3]][d+(3900*MainActivity.report_sequential)]);
							current_y=height-current_y;
							break;
						default:
							break;
						}
						if(d==0)
						{
							canvas.saveState();
							canvas.setColorStroke(new GrayColor(0.2f));
						    canvas.setColorFill(new BaseColor(0,0,0));
							canvas.circle(prev_x1,current_y, 1);
							canvas.fillStroke();
						    canvas.restoreState();
						}
						else
						{
							canvas.saveState();
							canvas.setColorStroke(new GrayColor(0.2f));
						    canvas.setColorFill(new BaseColor(0,0,0));
							canvas.moveTo(prev_x1, prev_y1);
							canvas.lineTo(x_offset[c]+d, current_y);
							canvas.fillStroke();
						    canvas.restoreState();
						}
						
						prev_x1=x_offset[c]+d;
						prev_y1=current_y;
					}
				}
			}
			
			//plot full lead II data 
			 prev_x1=xbase1 = 135;//5*25;
			 prev_y1 =ybase=(3420);//3425,5*685
			 paint.setColor(Color.BLACK);
			 paint.setStrokeWidth(3f); 
			for(int k = 0; k< PaintView.total_samples-1300; k++)//XAXIS_WD=1300
			{
				current_x1=(float) (xbase1+(k));
				current_y=(float) (ybase-Adjust_y_pixel(PaintView.disp_report[2][k]));
				current_y=height-current_y;
				
				if(k==0)
				{
					canvas.saveState();
					canvas.setColorStroke(new GrayColor(0.2f));
				    canvas.setColorFill(new BaseColor(0,0,0));
					canvas.circle(prev_x1,current_y, 1);
					canvas.fillStroke();
				    canvas.restoreState();
				}
				else
				{
					canvas.saveState();
					canvas.setColorStroke(new GrayColor(0.2f));
				    canvas.setColorFill(new BaseColor(0,0,0));
					canvas.moveTo(prev_x1, prev_y1);
					canvas.lineTo(current_x1, current_y);
					canvas.fillStroke();
				    canvas.restoreState();
				}
				prev_x1=current_x1;
				prev_y1=current_y;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void draw_grid_pdf(PdfContentByte canvas, int dirtn, int end_pt) {
		try {
			int x1=0,x2=0,y1=0,y2=0;
			if(dirtn==1)// vertical lines
			{
				y1=36; y2=3235; // y2=3035;3250
			}
			else ///horizontal lines
			{
				x1=36;x2=width-63;
			}
			
			for (int i = 36,j=0,k=0; i <=end_pt-5*4; i+=5*4,j++) //4 is the pixel on canvas
			{
				if(dirtn==1)//vertical
				{
					if(j%5==0)//dark pink line
					{
						canvas.setColorStroke(new BaseColor(255,51,153));
					}
					else
					{
						canvas.setColorStroke(new BaseColor(255, 185, 220));
					}
					canvas.saveState();
					canvas.moveTo(i, y1);
					canvas.lineTo(i, y2);
					canvas.fillStroke();
			        canvas.restoreState();
					
					//plots blue lines 
					if(j%5==0)
					{
						if((k-1)%13 == 0 || k==0)
						{
							canvas.saveState();
						    canvas.setColorStroke(new BaseColor(21,64,234));//blue
							 if((k)==14||k==27||k==40)
							 {
								 canvas.moveTo(i, 840);
								 canvas.lineTo(i, y2);//5*605,3025
								 canvas.fillStroke();
							     canvas.restoreState();
							 }
							 else 
							 {
								 canvas.moveTo(i, y1);
								 canvas.lineTo(i, y2);
								 canvas.fillStroke();
							     canvas.restoreState();
							 }
						}
						k++;
					}
				}
				else//horizontal
				{
					if(j%5==0)//dark line
					{
							canvas.setColorStroke(new BaseColor(255,51,153));
							if((k)==8 || k==0)//draws horztal blue lines
							{
								canvas.setColorStroke(new BaseColor(21,64,234));
							}
							k++;
					}
					else
					{
						canvas.setColorStroke(new BaseColor(255, 185, 220));
					}
					canvas.saveState();
					canvas.moveTo(x1, i);
					canvas.lineTo(x2, i);
					canvas.fillStroke();
				    canvas.restoreState();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	public void create_ascii_file() {

		try {
			sFileTitle_data= PaintView.Pass_On_date.replaceAll(" ","_")+"_"+ PaintView.Pass_On_name.replaceAll(" ", "_");

			
			File root = new File(Environment.getExternalStorageDirectory()+"/TELE-ECG/TELE-ECG Reports", PaintView.Pass_On_name);
	        if (!root.isDirectory()) 
	            root.mkdirs();
	        
	        File ECG_FILE_NAME = new File(root,sFileTitle_data+".txt");
	        
	        FileOutputStream fstream = new FileOutputStream(ECG_FILE_NAME);
			BufferedOutputStream bstream = new BufferedOutputStream(fstream);
			DataOutputStream dstream = new DataOutputStream(bstream);
			
			dstream.writeBytes("12ECG_2014\n");
			dstream.writeBytes("Patient Details:\n");
			dstream.writeBytes("Date:"+ PaintView.Pass_On_date+"\n");
			dstream.writeBytes("ChSS No:"+ PaintView.Pass_On_chno+"\n");
			dstream.writeBytes("Name:"+ PaintView.Pass_On_name+"\n");
			dstream.writeBytes("Date of birth:"+ PaintView.Pass_On_dob+"\n");
			dstream.writeBytes("Age:"+ PaintView.Pass_On_age+"\n");
			dstream.writeBytes("Gender:"+ PaintView.Pass_On_gen+"\n");
			dstream.writeBytes("Height:"+ PaintView.Pass_On_ht+"\n");
			dstream.writeBytes("Weight:"+ PaintView.Pass_On_wt+"\n");
			dstream.writeBytes("Medications:"+ PaintView.Pass_On_medi+"\n");
			dstream.writeBytes("Blood pressure:"+ PaintView.Pass_On_BP+"\n");
			dstream.writeBytes("Gain:\n");
			
			dstream.writeBytes(String.format("%20s %20s %20s %20s %20s %20s %20s %20s %20s %20s %20s %20s","lead I","lead II ","lead III","lead aVR","lead aVL","lead aVF","lead V1","lead V2","lead V3","lead V4","lead V5","lead V6\r\n"));
			
			for (int i = 0; i < PaintView.total_samples; i++) {
				dstream.writeBytes(String.format("%20s %20s %20s %20s %20s %20s %20s %20s %20s %20s %20s %20s", PaintView.raw_data[PaintView.Lead_I][i], PaintView.raw_data[PaintView.Lead_II][i], PaintView.raw_data[PaintView.Lead_III][i], PaintView.raw_data[PaintView.aVR][i], PaintView.raw_data[PaintView.aVL][i], PaintView.raw_data[PaintView.aVF][i], PaintView.raw_data[PaintView.V1][i], PaintView.raw_data[PaintView.V2][i], PaintView.raw_data[PaintView.V3][i], PaintView.raw_data[PaintView.V4][i], PaintView.raw_data[PaintView.V5][i], PaintView.raw_data[PaintView.V6][i]+"\r\n"));
			}
			
			
			dstream.close();
			bstream.close();
			fstream.close();
		
		//	Toast.makeText(getContext(), "text file created", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
