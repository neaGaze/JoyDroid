package com.android.joydroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.BitmapFactory.Options;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;

import com.android.joydroid.client.connection.activity.*;
import com.android.joydroid.PreferencesActivity;
import com.android.joydroid.client.app.Joydroid;
import com.android.joydroid.client.app.ProxySensor;
import com.android.joydroid.client.protocol.ButtonAction;
import com.android.joydroid.client.protocol.DirectionControlAction;
import org.techgaun.desktop.remotepcdroidserver.RemotePCDroidKeyCodeConverter;
//import com.android.joydroid.client.protocol.JoydroidActionReceiver;

public class JoyDroidActivity extends FragmentActivity  implements OnClickListener, OnLongClickListener{
 
	public SimulationView mSimulationView;
    private SensorManager mSensorManager;
    private PowerManager mPowerManager;
    private WindowManager mWindowManager;
    private Display mDisplay;
    private WakeLock mWakeLock;
	private SharedPreferences preferences;
	public Joydroid apps; 
	public ProxySensor pSensor;
	private Button y1,b2,a3,x4;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    
        apps=(Joydroid) this.getApplication();
        this.preferences = this.apps.getPreferences();
        
        //creates a proxy sensor for acclerometer direction control
        pSensor = new ProxySensor(this.apps);
        
        // Get an instance of the SensorManager
        
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get an instance of the PowerManager
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Get an instance of the WindowManager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();

        // Create a bright wake lock
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());

        // instantiate our simulation view and set it as the activity's content
        mSimulationView = new SimulationView(JoyDroidActivity.this);
       // setContentView(mSimulationView);
        
        setContentView(R.layout.main);
        FragmentManager fragmanager=getSupportFragmentManager();
        FragmentTransaction fragtrans = fragmanager.beginTransaction();
        Fragment simulation= new FragmentActivityModule(mSimulationView);
        if (null ==  fragmanager.findFragmentByTag("FRAG_TAG"))				//to check for any leakages of fragment or avoiding adding that fragment more than once
        {	fragtrans.add(R.id.linearLayout1, simulation, "FRAG_TAG").commit();	}
    
        buttonInitiative();     
        
    }
    
    private void buttonInitiative() {
    	  this.y1=(Button) this.findViewById(R.id.button_y1);
          this.y1.setOnClickListener(JoyDroidActivity.this);
          this.y1.setOnLongClickListener( JoyDroidActivity.this);
          
          this.b2=(Button) this.findViewById(R.id.button_b2);
          this.b2.setOnClickListener(JoyDroidActivity.this);
          this.b2.setOnLongClickListener(JoyDroidActivity.this);
          
          this.a3=(Button) this.findViewById(R.id.button_a3);
          this.a3.setOnClickListener(JoyDroidActivity.this);
          this.a3.setOnLongClickListener( JoyDroidActivity.this);
          
          this.x4=(Button) this.findViewById(R.id.button_x4);
          this.x4.setOnClickListener(JoyDroidActivity.this);
          this.x4.setOnLongClickListener( JoyDroidActivity.this);
          
	}

	@Override
    public void onResume()
    {super.onResume();
    mSimulationView.startSimulation();
    this.apps.registerActionReceiver();    //This is required. Don't remove it
    }
    
    @Override
    public void onPause()
    {super.onPause();
    mSimulationView.stopSimulation();
    this.apps.unregisterActionReceiver();
    }
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	menu.add(Menu.NONE, 0, Menu.NONE, this.getResources().getString(R.string.text_servers));
    	menu.add(Menu.NONE, 1, Menu.NONE, this.getResources().getString(R.string.text_preferences));
		menu.add(Menu.NONE, 2, Menu.NONE, this.getResources().getString(R.string.text_about));
		menu.add(Menu.NONE, 3, Menu.NONE, this.getResources().getString(R.string.text_help));
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case 0:
			this.startActivity(new Intent(this, ConnListActivity.class));
				break;
			case 1:
				this.startActivity(new Intent(this, PreferencesActivity.class)); 		 
				break;
			case 2:
				this.startActivity(new Intent(this, AboutActivity.class));
				break;
			case 3:
				this.startActivity(new Intent(this, HelpActivity.class));
				break;
		}
		
		return true;
	}
    
   /* For the gravity simulation   */ 
    /***************************************************************************************************************************/
    class SimulationView extends View implements SensorEventListener
    {

        // diameter of the balls in meters
        private static final float sBallDiameter = 0.004f;
        private static final float sBallDiameter2 = sBallDiameter * sBallDiameter;

        // friction of the virtual table and air
        private static final float sFriction = 0.1f;

        private Sensor mAccelerometer;
        private long mLastT;
        private float mLastDeltaT;

        private int viewWidth;
        private int viewHeight;
        private float mXDpi;
        private float mYDpi;
        private float mMetersToPixelsX;
        private float mMetersToPixelsY;
        private Bitmap mBitmap;
        private Bitmap mWood;
        private float mXOrigin;
        private float mYOrigin;
        private float mSensorX;
        private float mSensorY;
        private long mSensorTimeStamp;
        private long mCpuTimeStamp;
        private float mHorizontalBound;
        private float mVerticalBound;       
        private final ParticleSystem mParticleSystem = new ParticleSystem();
        
        /*
         * Each of our particle holds its previous and current position, its
         * acceleration. for added realism each particle has its own friction
         * coefficient.
         */     
        class Particle {
            private float mPosX;
            private float mPosY;
            private float mAccelX;
            private float mAccelY;
            private float mLastPosX;
            private float mLastPosY;
            private float mOneMinusFriction;
            private float friction; 
            private float test;
            Particle() {          	
        	 // make each particle a bit different by randomizing its
             // coefficient of friction
        		
     //   		friction=(float)Math.random();
         		friction=apps.getSensitivity();
        		Log.i ("TEST",  Float.toString(friction));   		

                final float r = (friction - 0.5f) * 0.2f;
                mOneMinusFriction = 1.0f - sFriction + r;
            }

            public void computePhysics(float sx, float sy, float dT, float dTC) {
                // Force of gravity applied to our virtual object
                final float m = 1000.0f; // mass of our virtual object
                final float gx = -sx * m;
                final float gy = -sy * m;

                /*
                 * �F = mA <=> A = �F / m We could simplify the code by
                 * completely eliminating "m" (the mass) from all the equations,
                 * but it would hide the concepts from this sample code.
                 */
                final float invm = 1.0f / m;
                final float ax = gx * invm;
                final float ay = gy * invm;

                /*
                 * Time-corrected Verlet integration The position Verlet
                 * integrator is defined as x(t+�t) = x(t) + x(t) - x(t-�t) +
                 * a(t)�t�2 However, the above equation doesn't handle variable
                 * �t very well, a time-corrected version is needed: x(t+�t) =
                 * x(t) + (x(t) - x(t-�t)) * (�t/�t_prev) + a(t)�t�2 We also add
                 * a simple friction term (f) to the equation: x(t+�t) = x(t) +
                 * (1-f) * (x(t) - x(t-�t)) * (�t/�t_prev) + a(t)�t�2
                 */
                final float dTdT = dT * dT;		//dT = (float) (t - mLastT) * (1.0f / 1000000000.0f);
                final float x = mPosX + mOneMinusFriction * dTC * (mPosX - mLastPosX) + mAccelX	
                        * dTdT;			//dTC = dT / mLastDeltaT;
                final float y = mPosY + mOneMinusFriction * dTC * (mPosY - mLastPosY) + mAccelY
                        * dTdT;
                mLastPosX = mPosX;
                mLastPosY = mPosY;
                mPosX = x;
                mPosY = y;
                mAccelX = ax;
                mAccelY = ay;
            }

            /*
             * Resolving constraints and collisions with the Verlet integrator
             * can be very simple, we simply need to move a colliding or
             * constrained particle in such way that the constraint is
             * satisfied.
             */
            public void resolveCollisionWithBounds() {
                final float xmax = mHorizontalBound;
                final float ymax = mVerticalBound;
                final float x = mPosX;
                final float y = mPosY;
                if (x > xmax) {
                    mPosX = xmax;
                } else if (x < -xmax) {
                    mPosX = -xmax;
                }
                if (y > ymax) {
                    mPosY = ymax;
                } else if (y < -ymax) {
                    mPosY = -ymax;
                }
            }
        }

        /*
         * A particle system is just a collection of particles
         */
        class ParticleSystem {
            static final int NUM_PARTICLES = 1;
            private Particle mBalls[] = new Particle[NUM_PARTICLES];

            ParticleSystem() {
                /*
                 * Initially our particles have no speed or acceleration
                 */
                for (int i = 0; i < mBalls.length; i++) {
                    mBalls[i] = new Particle();
                }
            }

            /*
             * Update the position of each particle in the system using the
             * Verlet integrator.
             */
            private void updatePositions(float sx, float sy, long timestamp) {
                final long t = timestamp;
                if (mLastT != 0) {
                    final float dT = (float) (t - mLastT) * (1.0f / 1000000000.0f);
                    if (mLastDeltaT != 0) {
                        final float dTC = dT / mLastDeltaT;
                        final int count = mBalls.length;
                        for (int i = 0; i < count; i++) {
                            Particle ball = mBalls[i];
                            ball.computePhysics(sx, sy, dT, dTC);
                        }
                    }
                    mLastDeltaT = dT;
                }
                mLastT = t;
            }

            /*
             * Performs one iteration of the simulation. First updating the
             * position of all the particles and resolving the constraints and
             * collisions.
             */
            public void update(float sx, float sy, long now) {
                // update the system's positions
                updatePositions(sx, sy, now);

                // We do no more than a limited number of iterations
                final int NUM_MAX_ITERATIONS = 10;

                /*
                 * Resolve collisions, each particle is tested against every
                 * other particle for collision. If a collision is detected the
                 * particle is moved away using a virtual spring of infinite
                 * stiffness.
                 */
                boolean more = true;
                final int count = mBalls.length;
                for (int k = 0; k < NUM_MAX_ITERATIONS && more; k++) {
                    more = false;
                    for (int i = 0; i < count; i++) {
                        Particle curr = mBalls[i];
                        for (int j = i + 1; j < count; j++) {
                            Particle ball = mBalls[j];
                            float dx = ball.mPosX - curr.mPosX;
                            float dy = ball.mPosY - curr.mPosY;
                            float dd = dx * dx + dy * dy;		//for distance formulae
                            // Check for collisions
                            if (dd <= sBallDiameter2) {
                                /*
                                 * add a little bit of entropy, after nothing is
                                 * perfect in the universe.
                                 */
                                dx += ((float) Math.random() - 0.5f) * 0.0001f;
                                dy += ((float) Math.random() - 0.5f) * 0.0001f;
                                dd = dx * dx + dy * dy;
                                // simulate the spring
                                final float d = (float) Math.sqrt(dd);
                                final float c = (0.5f * (sBallDiameter - d)) / d;
                                curr.mPosX -= dx * c;
                                curr.mPosY -= dy * c;
                                ball.mPosX += dx * c;
                                ball.mPosY += dy * c;
                                more = true;
                            }
                        }
                        /*
                         * Finally make sure the particle doesn't intersects
                         * with the walls.
                         */
                        curr.resolveCollisionWithBounds();
                    }
                }
            }

            public int getParticleCount() {
                return mBalls.length;
            }

            public float getPosX(int i) {
                return mBalls[i].mPosX;
            }

            public float getPosY(int i) {
                return mBalls[i].mPosY;
            }
        }

        public void startSimulation() {
            /*
             * It is not necessary to get accelerometer events at a very high
             * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
             * automatic low-pass filter, which "extracts" the gravity component
             * of the acceleration. As an added benefit, we use less power and
             * CPU resources.
             */
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }

        public void stopSimulation() {
            mSensorManager.unregisterListener(this);
        }

        //Now this is the constructor of SimulationView
        public SimulationView(Context context) {
            super(context);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            mXDpi = metrics.xdpi;
            mYDpi = metrics.ydpi;
            mMetersToPixelsX = mXDpi / 0.0254f;
            mMetersToPixelsY = mYDpi / 0.0254f;

            // rescale the ball so it's about 0.5 cm on screen
            Bitmap ball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
            final int dstWidth = (int) (sBallDiameter * mMetersToPixelsX + 0.5f);
            final int dstHeight = (int) (sBallDiameter * mMetersToPixelsY + 0.5f);
            mBitmap = Bitmap.createScaledBitmap(ball, dstWidth, dstHeight, true);

            Options opts = new Options();
            opts.inDither = true;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            mWood = BitmapFactory.decodeResource(getResources(), R.drawable.wood, opts);           
  /*         
            Bitmap grid=BitmapFactory.decodeResource(getResources(), R.drawable.grid);
            final int gridWidth= (int) (0.012* mMetersToPixelsX);
            final int gridHeight= (int) (0.015 * mMetersToPixelsY);
            mGrid = Bitmap.createScaledBitmap(grid,gridWidth,gridHeight,true);			*/
           
        }
     
        
        Bitmap drawGridbitmap()
        {
        Bitmap grid1=BitmapFactory.decodeResource(getResources(), R.drawable.grid);
        final int gridWidth1= /* (int) (0.013* mMetersToPixelsX);	*/	viewWidth/3;
        final int gridHeight1= /*(int) (0.015 * mMetersToPixelsY);	*/	viewHeight/3;
        grid1 = Bitmap.createScaledBitmap(grid1,gridWidth1,gridHeight1,true);
        return grid1;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            // compute the origin of the screen relative to the origin of
            // the bitmap
        	super.onSizeChanged(w, h, oldw, oldh);
        	viewWidth=w;
        	viewHeight=h;
            mXOrigin = (w - mBitmap.getWidth()) * 0.5f;
            mYOrigin = (h - mBitmap.getHeight()) * 0.5f;
            mHorizontalBound = ((w / mMetersToPixelsX - sBallDiameter) * 0.5f);
            mVerticalBound = ((h / mMetersToPixelsY - sBallDiameter) * 0.5f);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
                return;
            /*
             * record the accelerometer data, the event's timestamp as well as
             * the current time. The latter is needed so we can calculate the
             * "present" time during rendering. In this application, we need to
             * take into account how the screen is rotated with respect to the
             * sensors (which always return data in a coordinate space aligned
             * to with the screen in its native orientation).
             */

            switch (mDisplay.getOrientation()) {
                case Surface.ROTATION_0:
                    mSensorX = event.values[0];
                    mSensorY = event.values[1];
                    break;
                case Surface.ROTATION_90:
                    mSensorX = -event.values[1];
                    mSensorY = event.values[0];
                    break;
                case Surface.ROTATION_180:
                    mSensorX = -event.values[0];
                    mSensorY = -event.values[1];
                    break;
                case Surface.ROTATION_270:
                    mSensorX = event.values[1];
                    mSensorY = -event.values[0];
                    break;
            }

            mSensorTimeStamp = event.timestamp;
            mCpuTimeStamp = System.nanoTime();
        }

        @Override
        public void onDraw(Canvas canvas) {

            /*
             * draw the background
             */

            canvas.drawBitmap(mWood, 0, 0, null);
       
            /*Draw a grid view on the different surfaces of the view*/                       
 //           Bitmap tmpGridBitmap=drawGridbitmap();		//FOR NOW THIS IS NOT CORRECT BECAUSE EVERY TIME THE onDraw() IS CALLED, tmpGridBitmap GETS INITIALIAZED
  //          canvas.drawBitmap(mGrid, 0, 0, null);			//draw bitmap into the canvas
  //          mGrid.eraseColor(android.graphics.Color.TRANSPARENT);		//remove bitmap from the canvas 
            
            /*
             * compute the new position of our object, based on accelerometer
             * data and present time.
             */
            final ParticleSystem particleSystem = mParticleSystem;
            final long now = mSensorTimeStamp + (System.nanoTime() - mCpuTimeStamp);
            final float sx = mSensorX;
            final float sy = mSensorY;

            particleSystem.update(sx, sy, now);

            final float xc = mXOrigin;
            final float yc = mYOrigin;
            final float xs = mMetersToPixelsX;
            final float ys = mMetersToPixelsY;
            final Bitmap bitmap = mBitmap;
            final int count = particleSystem.getParticleCount();
            for (int i = 0; i < count; i++) {
                /*
                 * We transform the canvas so that the coordinate system matches
                 * the sensors coordinate system with the origin in the center
                 * of the screen and the unit is the meter.
                 */

                final float x = xc + particleSystem.getPosX(i) * xs;
                final float y = yc - particleSystem.getPosY(i) * ys;
                
                /*draw the grid bitmap according to the position of the ball*/              
                
                if((x < (viewWidth/3)) && (y < (viewHeight/3))) 															//1st grid
                {
                	 Bitmap tmpGridBitmap=drawGridbitmap();		
                     canvas.drawBitmap(tmpGridBitmap, 0, 0, null);	     
         //			JoyDroidActivity.this.apps.sendAction(new DirectionControlAction(DirectionControlAction.DIR_UP_LEFT ));   
                     JoyDroidActivity.this.pSensor.sendViaProxy(DirectionControlAction.DIR_UP_LEFT );
                     //for_key_board = true;
                }
                else if(((x >= (viewWidth/3)) && (x < (2*viewWidth/3))) &&(y < (viewHeight/3)))			//2nd grid
                {
                	 Bitmap tmpGridBitmap=drawGridbitmap();		
                     canvas.drawBitmap(tmpGridBitmap, (viewWidth/3), 0, null);		
      //    			JoyDroidActivity.this.apps.sendAction(new DirectionControlAction(DirectionControlAction.DIR_UP ));  
          			JoyDroidActivity.this.pSensor.sendViaProxy(DirectionControlAction.DIR_UP );
                }
                else if((x >= (2*viewWidth/3)) && (y< (viewHeight/3)))												//3rd grid
                {
                	 Bitmap tmpGridBitmap=drawGridbitmap();		
                     canvas.drawBitmap(tmpGridBitmap, (2*viewWidth/3), 0, null);
          //			JoyDroidActivity.this.apps.sendAction(new DirectionControlAction(DirectionControlAction.DIR_UP_RIGHT )); 
                     JoyDroidActivity.this.pSensor.sendViaProxy(DirectionControlAction.DIR_UP_RIGHT );
                }
                else if((x < (viewWidth/3)) && ((y >=(viewHeight/3)) && (y < (2*viewHeight/3))))				//4th grid
                {
                	Bitmap tmpGridBitmap=drawGridbitmap();		
                    canvas.drawBitmap(tmpGridBitmap, 0, (viewHeight/3), null);
       //  			JoyDroidActivity.this.apps.sendAction(new DirectionControlAction(DirectionControlAction.DIR_LEFT ));   
          			JoyDroidActivity.this.pSensor.sendViaProxy(DirectionControlAction.DIR_LEFT );
                }
               else if(((x >=(viewWidth/3)) && (x < (2*viewWidth/3))) && ((y >=(viewHeight/3)) && (y < (2*viewHeight/3))))			//5th grid
                {
                	Bitmap tmpGridBitmap=drawGridbitmap();		
                    canvas.drawBitmap(tmpGridBitmap, (viewWidth/3),(viewHeight/3), null);
         //			JoyDroidActivity.this.apps.sendAction(new DirectionControlAction(DirectionControlAction.DIR_CENTER ));   
          			JoyDroidActivity.this.pSensor.sendViaProxy(DirectionControlAction.DIR_CENTER );
                }
               else if((x >= (2*viewWidth/3)) && ((y >=(viewHeight/3)) && (y < (2*viewHeight/3))))								//6th grid
               {
            	   Bitmap tmpGridBitmap=drawGridbitmap();		
                    canvas.drawBitmap(tmpGridBitmap, (2*viewWidth/3), (viewHeight/3), null);
         //			JoyDroidActivity.this.apps.sendAction(new DirectionControlAction(DirectionControlAction.DIR_RIGHT ));   
          			JoyDroidActivity.this.pSensor.sendViaProxy(DirectionControlAction.DIR_RIGHT );
               }
               else if((x < (viewWidth/3)) && (y >= (2*viewHeight/3)))													//7th grid
               {
            	   Bitmap tmpGridBitmap=drawGridbitmap();		
                   canvas.drawBitmap(tmpGridBitmap, 0, (2*viewHeight/3), null);
         //		   JoyDroidActivity.this.apps.sendAction(new DirectionControlAction(DirectionControlAction.DIR_DOWN_LEFT ));  
                   JoyDroidActivity.this.pSensor.sendViaProxy(DirectionControlAction.DIR_DOWN_LEFT );
               }
                else if(((x >=(viewWidth/3)) &&(x < (2*viewWidth/3))) && (y > (2*viewHeight/3)))									//8th grid
                {
                	Bitmap tmpGridBitmap=drawGridbitmap();		
                   canvas.drawBitmap(tmpGridBitmap, (viewWidth/3), (2*viewHeight/3), null);
        //			JoyDroidActivity.this.apps.sendAction(new DirectionControlAction(DirectionControlAction.DIR_DOWN ));   
          			JoyDroidActivity.this.pSensor.sendViaProxy(DirectionControlAction.DIR_DOWN );
                }
                else if(((x >= (2*viewWidth/3)) && (x < 337)) && (y > (2*viewHeight/3)))									//9th grid
                {
                	Bitmap tmpGridBitmap=drawGridbitmap();		
                    canvas.drawBitmap(tmpGridBitmap, (2*viewWidth/3), (2*viewHeight/3), null);
         //			JoyDroidActivity.this.apps.sendAction(new DirectionControlAction(DirectionControlAction.DIR_DOWN_RIGHT ));   
                    JoyDroidActivity.this.pSensor.sendViaProxy(DirectionControlAction.DIR_DOWN_RIGHT );
                }
                
                /*draw the ball into the canvas according to the accelerometer data*/
                canvas.drawBitmap(bitmap, x, y, null);
               
            }

            // and make sure to redraw asap
            invalidate();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    
    }
    
	@Override
	public void onClick(View arg0)
	{
		if(arg0==JoyDroidActivity.this.y1)
		{
	//		Toast.makeText(JoyDroidActivity.this, "Y1 pressed", Toast.LENGTH_SHORT).show();
			this.apps.sendAction(new ButtonAction(ButtonAction.BUT_Y ));
		}
		else if(arg0==JoyDroidActivity.this.b2)
		{
	//		Toast.makeText(JoyDroidActivity.this, "B2 pressed", Toast.LENGTH_SHORT).show();
			this.apps.sendAction(new ButtonAction(ButtonAction.BUT_B));
		}
		else if(arg0==JoyDroidActivity.this.a3)
		{
	//		Toast.makeText(JoyDroidActivity.this, "A3 pressed", Toast.LENGTH_SHORT).show();
			this.apps.sendAction(new ButtonAction(ButtonAction.BUT_A));
		}
		else if(arg0==JoyDroidActivity.this.x4)
		{
	//		Toast.makeText(JoyDroidActivity.this, "X4 pressed", Toast.LENGTH_SHORT).show();
			this.apps.sendAction(new ButtonAction(ButtonAction.BUT_X ));
		}
	}
	/*
	private void checkOnCreate()
	{
		if (this.isFirstTimeRun())
		{
			this.firstRunDialog();
		}
	}
	
	private boolean isFirstTimeRun()
	{
		return this.preferences.getBoolean("firstRun", true);
	}
	
	private void firstRunDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setMessage(R.string.text_first_run_dialog);
		builder.setPositiveButton(R.string.text_yes, new OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				JoyDroidActivity.this.startActivity(new Intent(JoyDroidActivity.this, HelpActivity.class));
				JoyDroidActivity.this.disableFirstRun();
			}
		});
		builder.setNegativeButton(R.string.text_no, new OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.cancel();
				JoyDroidActivity.this.disableFirstRun();
			}
		});
		builder.create().show();
	}
	
	private void disableFirstRun()
	{
		Editor editor = this.preferences.edit();
		editor.putBoolean("firstRun", false);
		editor.commit();
	}
*/

	@Override
	public boolean onLongClick(View v) {

		if(v==JoyDroidActivity.this.y1)
		{
	//		Toast.makeText(JoyDroidActivity.this, "Y1 pressed", Toast.LENGTH_SHORT).show();
			this.apps.sendAction(new ButtonAction(ButtonAction.BUT_Y ));
		}
		else if(v==JoyDroidActivity.this.b2)
		{
	//		Toast.makeText(JoyDroidActivity.this, "B2 pressed", Toast.LENGTH_SHORT).show();
			this.apps.sendAction(new ButtonAction(ButtonAction.BUT_B));
		}
		else if(v==JoyDroidActivity.this.a3)
		{
	//		Toast.makeText(JoyDroidActivity.this, "A3 pressed", Toast.LENGTH_SHORT).show();
			this.apps.sendAction(new ButtonAction(ButtonAction.BUT_A));
		}
		else if(v==JoyDroidActivity.this.x4)
		{
	//		Toast.makeText(JoyDroidActivity.this, "X4 pressed", Toast.LENGTH_SHORT).show();
			this.apps.sendAction(new ButtonAction(ButtonAction.BUT_X ));
		}
	
		return false;
	}
}
  