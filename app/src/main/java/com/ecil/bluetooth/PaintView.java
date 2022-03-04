package com.ecil.bluetooth;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;


public class PaintView extends View {

    public PaintView(Context context) {
        super(context);
    }

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PaintView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    int cal_width = 25, i, s, e;
    static int CH_NUM = 12, BATT_LOW_LEVEL = 147, Battery_level = 0, batt_count = 0, BATT_index = 0;
    float adjust_Height = 0, start_position;
    public static float Max_Scaling_factor = 0, Min_Scaling_factor = 0, max_value = 0, min_value = 0;
    float Load_Max_Scaling_factor = 0, Load_Min_Scaling_factor = 0;
    public int temp_count = 0;
    public static int total_samples = 6500;
    public static int XAXIS_WD = total_samples / 5;//1300
    public static int disp_samples;//=XAXIS_WD/MainActivity.step;//260
    public static int acquired_pts, index = 0, plot_pt, check_data_count = 0;
    public static boolean START_GUI = false, roll_over = false, start_acq = false;
    public static int avg_pt_to_display = 0, r_step = 0, disp_count_report = 0;
    public static int iScreenWidth;
    public int iScreenHeight_bmp, iStatus_Height, ibitmapheight;
    protected static final int MESSAGE_COMMUNICATION = 0;

    //ECG DATA DECLARATION
    public static final int ORDER = 200;            // order of FIR filter
    public static short raw_data[][] = new short[CH_NUM][total_samples],
            temp_ADS_data[][] = new short[8][ORDER + 1],
            Gen_report[][] = new short[CH_NUM][total_samples],
            final_data[][] = new short[CH_NUM][total_samples];
    public static boolean plot_on = false,
            generate_report_opt = false,
            communication_error = false;
    public static int ADS_samples_count = 0,
            disp_count = 0,
            fill_count = 0;
    int temp_index = 0;
    public static float display_data[][] = new float[CH_NUM][MainActivity.iScreenWidth],
            display_load_data[][] = new float[CH_NUM][XAXIS_WD],
            disp_report[][] = new float[CH_NUM][total_samples],
            display_data_report[][] = new float[CH_NUM][XAXIS_WD],
            cal_display[][] = new float[CH_NUM][MainActivity.iScreenWidth];
    public static float pre_display_data[][] = new float[CH_NUM][MainActivity.iScreenWidth];

    public short AVE[] = new short[CH_NUM];
    public double DC[] = new double[CH_NUM];
    int FIR_count;
    float FIR_data;
    float Scaling_Factor;
    String slead_names = "";
    public static int chk_disp_count = 0;
    public double FIR_B[] = new double[201];
    public static String txt_filter;

    double FIR_B_150[] = {
            -0.002449142, -0.004108513, 0.000432584, 0.002179089, 0.000284631, 0.003138018, 0.004877818, -0.000339571, -0.00253275, -0.00066628, -0.003885438, -0.005713726, 0.000242016, 0.002916586, 0.001069306, 0.004685094,
            0.006614095, -0.00014411, -0.003330545, -0.001484032, -0.005529271, -0.00757648, 5.10E-05, 0.003774725, 0.001898729, 0.00640895, 0.008598499, 3.10E-05, -0.004249615, -0.002299375, -0.007313944, -0.009678357,
            -9.44E-05, 0.00475643, 0.002669289, 0.008233074, 0.010815574, 0.000129901, -0.005297597, -0.002988537, -0.009154362, -0.012012038, -0.000126003, 0.005877475, 0.003232953, 0.010065261, 0.01327357, 6.82E-05, -0.006503453,
            -0.00337251, -0.010952897, -0.014612353, 6.25E-05, 0.007187714, 0.003368501, 0.011804327, 0.016050882, -0.000292192, -0.007950207, -0.003168515, -0.012606804, -0.017628804, 0.000658424, 0.008823947, 0.002697058,
            0.013348044, 0.01941559, -0.001219068, -0.009865237, -0.00183688, -0.014016485, -0.021536111, 0.00207046, 0.011175117, 0.000388499, 0.014601536, 0.024227883, -0.003389421, -0.012949983, 0.002028391, -0.015093809,
            -0.027988394, 0.00554544, 0.015621371, -0.006258758, 0.015485319, 0.0340377, -0.00947189, -0.020345043, 0.014670653, -0.015769666, -0.046322463, 0.018429559, 0.031585884, -0.037506481, 0.015942175, 0.088549176,
            -0.057405949, -0.098475544, 0.289776277, 0.583999869, 0.289776277, -0.098475544, -0.057405949, 0.088549176, 0.015942175, -0.037506481, 0.031585884, 0.018429559, -0.046322463, -0.015769666, 0.014670653, -0.020345043,
            -0.00947189, 0.0340377, 0.015485319, -0.006258758, 0.015621371, 0.00554544, -0.027988394, -0.015093809, 0.002028391, -0.012949983, -0.003389421, 0.024227883, 0.014601536, 0.000388499, 0.011175117, 0.00207046, -0.021536111,
            -0.014016485, -0.00183688, -0.009865237, -0.001219068, 0.01941559, 0.013348044, 0.002697058, 0.008823947, 0.000658424, -0.017628804, -0.012606804, -0.003168515, -0.007950207, -0.000292192, 0.016050882, 0.011804327,
            0.003368501, 0.007187714, 6.25E-05, -0.014612353, -0.010952897, -0.00337251, -0.006503453, 6.82E-05, 0.01327357, 0.010065261, 0.003232953, 0.005877475, -0.000126003, -0.012012038, -0.009154362, -0.002988537, -0.005297597,
            0.000129901, 0.010815574, 0.008233074, 0.002669289, 0.00475643, -9.44E-05, -0.009678357, -0.007313944, -0.002299375, -0.004249615, 3.10E-05, 0.008598499, 0.00640895, 0.001898729, 0.003774725, 5.10E-05, -0.00757648,
            -0.005529271, -0.001484032, -0.003330545, -0.00014411, 0.006614095, 0.004685094, 0.001069306, 0.002916586, 0.000242016, -0.005713726, -0.003885438, -0.00066628, -0.00253275, -0.000339571, 0.004877818, 0.003138018,
            0.000284631, 0.002179089, 0.000432584, -0.004108513, -0.002449142
    };      // Multiband Hanning Window (0-48-52-150) of order 200 (Truncated version from order 500)
    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------

    double FIR_B_40[] = {
            -2.26281078534108E-21, -5.91481263388733E-07, -1.20511967943972E-06, -1.75790430653927E-06, -2.02763334573970E-06, -1.69282564594346E-06, -4.63248147966531E-07, 1.73395585920674E-06, 4.58223222409027E-06, 7.30319534207313E-06,
            8.75593453271818E-06, 7.73455235146200E-06, 3.42022894627932E-06, -4.13202525730964E-06, -1.36473359114509E-05, -2.26089708505586E-05, -2.76859702736527E-05, -2.56131022063575E-05, -1.43622659141391E-05, 5.71313650069656E-06,
            3.11724602042011E-05, 5.56083031569827E-05, 7.08134712546820E-05, 6.89209970084714E-05, 4.50898472200466E-05, -1.57209007060853E-19, -5.87252802849690E-05, -1.16941378279071E-04, -1.56621965509162E-04, -1.60463373595145E-04,
            -1.17495583918255E-04, -2.81706811788007E-05, 9.28175491430177E-05, 2.17436941501011E-04, 3.09570001513650E-04, 3.34023176604077E-04, 2.67385641195138E-04, 1.07820681076856E-04, -1.19449616041356E-04, -3.63591394512667E-04,
            -5.57605982870859E-04, -6.34633348552898E-04, -5.47651750332237E-04, -2.87381800519346E-04, 1.07298480871724E-04, 5.51002484619006E-04, 9.27414785261367E-04, 1.11689152936688E-03, 1.03026160399811E-03, 6.40112075836054E-04,
            -1.41570623705223E-18, -7.56144358758569E-04, -1.43775016573430E-03, -1.84166782295351E-03, -1.80740225051867E-03, -1.26960995695561E-03, -2.92443293756717E-04, 9.26989220786466E-04, 2.09201468779404E-03, 2.87308186708934E-03,
            2.99420265579887E-03, 2.31795487143759E-03, 9.05043594340042E-04, -9.72040929192355E-04, -2.87192713450818E-03, -4.28028827776975E-03, -4.74002254421673E-03, -3.98476657743714E-03, -2.03954307818162E-03, 7.43677795407076E-04,
            3.73435700713749E-03, 6.15424750724003E-03, 7.26667914721248E-03, 6.58117766641202E-03, 4.02046632890138E-03, -4.37816610139955E-18, -4.61300368791347E-03, -8.66637543182328E-03, -1.09884601834087E-02, -1.06956955508987E-02,
            -7.46761895394357E-03, -1.71366140281108E-03, 5.42555905418073E-03, 1.22646947601689E-02, 1.69255710905347E-02, 1.77884160818949E-02, 1.39442506138748E-02, 5.53904025465503E-03, -6.08555948909184E-03, -1.85115951955270E-02,
            -2.86257100639776E-02, -3.32025367403776E-02, -2.95801608463445E-02, -1.62874548007747E-02, 6.51672569815045E-03, 3.69006347660396E-02, 7.13632962560817E-02, 1.05363072326158E-01, 1.34079178108676E-01, 1.53261366099286E-01,
            1.60000000000000E-01, 1.53261366099286E-01, 1.34079178108676E-01, 1.05363072326158E-01, 7.13632962560817E-02, 3.69006347660396E-02, 6.51672569815045E-03, -1.62874548007747E-02, -2.95801608463445E-02, -3.32025367403776E-02,
            -2.86257100639776E-02, -1.85115951955270E-02, -6.08555948909184E-03, 5.53904025465503E-03, 1.39442506138748E-02, 1.77884160818949E-02, 1.69255710905347E-02, 1.22646947601689E-02, 5.42555905418073E-03, -1.71366140281108E-03,
            -7.46761895394358E-03, -1.06956955508987E-02, -1.09884601834087E-02, -8.66637543182328E-03, -4.61300368791348E-03, -4.37816610139955E-18, 4.02046632890138E-03, 6.58117766641202E-03, 7.26667914721248E-03, 6.15424750724003E-03,
            3.73435700713749E-03, 7.43677795407077E-04, -2.03954307818162E-03, -3.98476657743714E-03, -4.74002254421673E-03, -4.28028827776975E-03, -2.87192713450818E-03, -9.72040929192355E-04, 9.05043594340042E-04, 2.31795487143759E-03,
            2.99420265579888E-03, 2.87308186708934E-03, 2.09201468779404E-03, 9.26989220786466E-04, -2.92443293756717E-04, -1.26960995695561E-03, -1.80740225051867E-03, -1.84166782295351E-03, -1.43775016573430E-03, -7.56144358758569E-04,
            -1.41570623705223E-18, 6.40112075836054E-04, 1.03026160399811E-03, 1.11689152936688E-03, 9.27414785261368E-04, 5.51002484619006E-04, 1.07298480871724E-04, -2.87381800519346E-04, -5.47651750332237E-04, -6.34633348552898E-04,
            -5.57605982870860E-04, -3.63591394512668E-04, -1.19449616041357E-04, 1.07820681076856E-04, 2.67385641195139E-04, 3.34023176604078E-04, 3.09570001513649E-04, 2.17436941501010E-04, 9.28175491430176E-05, -2.81706811788007E-05,
            -1.17495583918255E-04, -1.60463373595145E-04, -1.56621965509163E-04, -1.16941378279072E-04, -5.87252802849691E-05, -1.57209007060853E-19, 4.50898472200466E-05, 6.89209970084711E-05, 7.08134712546818E-05, 5.56083031569830E-05,
            3.11724602042011E-05, 5.71313650069655E-06, -1.43622659141391E-05, -2.56131022063576E-05, -2.76859702736528E-05, -2.26089708505587E-05, -1.36473359114510E-05, -4.13202525730963E-06, 3.42022894627936E-06, 7.73455235146197E-06,
            8.75593453271796E-06, 7.30319534207316E-06, 4.58223222409027E-06, 1.73395585920678E-06, -4.63248147966534E-07, -1.69282564594341E-06, -2.02763334573989E-06, -1.75790430653919E-06, -1.20511967943972E-06, -5.91481263388733E-07,
            -2.26281078534108E-21
    };    // LPF Nuttall Window (0-40) of order 200 *

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------

    double FIR_B_5_40[] = {
            -1.97995943717345E-21, -5.14389135571994E-07, -1.02623000672594E-06, -1.42785460897858E-06, -1.47034241750163E-06, -8.02854536477813E-07, 8.97388711088801E-07, 3.73947951133474E-06, 7.44721505240634E-06, 1.12870073587166E-05,
            1.41673996772070E-05, 1.49366706221988E-05, 1.28347968383667E-05, 7.97991389160676E-06, 1.71384662414205E-06, -3.37663152052322E-06, -3.88841224841857E-06, 3.51674438682876E-06, 2.09392825401826E-05, 4.80956700710307E-05,
            8.16105603275518E-05, 1.15134889933976E-04, 1.40510171765760E-04, 1.49905567453302E-04, 1.38500391084114E-04, 1.06975746782874E-04, 6.29331574386767E-05, 2.04688116227515E-05, -2.47002195750658E-06, 1.13066030192392E-05, 7.26162643894975E-05,
            1.80811694896774E-04, 3.20957261084848E-04, 4.64728530726931E-04, 5.75661816287564E-04, 6.18160261790652E-04, 5.68349849040918E-04, 4.23868489808764E-04, 2.09348992931431E-04, -2.50290438412185E-05, -2.12986533126377E-04, -2.88447899347865E-04,
            -2.05239409824855E-04, 4.50090127025914E-05, 4.22451965317525E-04, 8.40681633638162E-04, 1.18231286514645E-03, 1.32658992427061E-03, 1.18319509624220E-03, 7.23542617126014E-04, -1.59266951668376E-18, -8.54698245688730E-04, -1.65117183744076E-03,
            -2.18744427148483E-03, -2.30416310721274E-03, -1.93708341158340E-03, -1.15139788734895E-03, -1.45182713512623E-04, 7.84008924662986E-04, 1.30584759074836E-03, 1.14368364530997E-03, 1.59564266302112E-04, -1.58619149490220E-03,
            -3.82132181483234E-03, -6.10451386266856E-03, -7.92131896123435E-03, -8.81432300838584E-03, -8.51665178896722E-03, -7.05261199290101E-03, -4.77324782426069E-03, -2.30795955653730E-03, -4.33641845409937E-04, 1.14599871055254E-04,
            -1.15193516522591E-03, -4.30854717467240E-03, -8.93760981625029E-03, -1.41695493384689E-02, -1.88496682181069E-02, -2.18036257855372E-02, -2.21450333872561E-02, -1.95504802364572E-02, -1.44263476696228E-02, -7.91012467343988E-03,
            -1.68397393281349E-03, 2.37714616065113E-03, 2.65668556252858E-03, -1.75113348098428E-03, -1.06971912129768E-02, -2.28367518423746E-02, -3.57488821023752E-02, -4.63173718356157E-02, -5.13141520887100E-02, -4.80747828425986E-02,
            -3.51258096888066E-02, -1.26239815538454E-02, 1.75008230125876E-02, 5.17492384698247E-02, 8.55809658565660E-02, 1.14176274937478E-01, 1.33285678692044E-01, 1.40000000000000E-01, 1.33285678692044E-01, 1.14176274937478E-01, 8.55809658565660E-02,
            5.17492384698247E-02, 1.75008230125876E-02, -1.26239815538454E-02, -3.51258096888066E-02, -4.80747828425986E-02, -5.13141520887100E-02, -4.63173718356157E-02, -3.57488821023752E-02, -2.28367518423746E-02, -1.06971912129768E-02,
            -1.75113348098428E-03, 2.65668556252858E-03, 2.37714616065113E-03, -1.68397393281349E-03, -7.91012467343988E-03, -1.44263476696228E-02, -1.95504802364572E-02, -2.21450333872561E-02, -2.18036257855372E-02, -1.88496682181069E-02,
            -1.41695493384689E-02, -8.93760981625030E-03, -4.30854717467239E-03, -1.15193516522591E-03, 1.14599871055254E-04, -4.33641845409937E-04, -2.30795955653730E-03, -4.77324782426069E-03, -7.05261199290101E-03, -8.51665178896721E-03,
            -8.81432300838584E-03, -7.92131896123435E-03, -6.10451386266855E-03, -3.82132181483234E-03, -1.58619149490220E-03, 1.59564266302112E-04, 1.14368364530997E-03, 1.30584759074836E-03, 7.84008924662986E-04, -1.45182713512623E-04,
            -1.15139788734895E-03, -1.93708341158340E-03, -2.30416310721274E-03, -2.18744427148482E-03, -1.65117183744076E-03, -8.54698245688730E-04, -1.59266951668376E-18, 7.23542617126014E-04, 1.18319509624220E-03, 1.32658992427061E-03,
            1.18231286514645E-03, 8.40681633638163E-04, 4.22451965317526E-04, 4.50090127025915E-05, -2.05239409824855E-04, -2.88447899347865E-04, -2.12986533126377E-04, -2.50290438412185E-05, 2.09348992931431E-04, 4.23868489808765E-04,
            5.68349849040919E-04, 6.18160261790654E-04, 5.75661816287563E-04, 4.64728530726931E-04, 3.20957261084848E-04, 1.80811694896774E-04, 7.26162643894976E-05, 1.13066030192392E-05, -2.47002195750659E-06, 2.04688116227515E-05, 6.29331574386768E-05,
            1.06975746782874E-04, 1.38500391084114E-04, 1.49905567453301E-04, 1.40510171765760E-04, 1.15134889933977E-04, 8.16105603275518E-05, 4.80956700710307E-05, 2.09392825401826E-05, 3.51674438682878E-06, -3.88841224841858E-06, -3.37663152052323E-06,
            1.71384662414206E-06, 7.97991389160675E-06, 1.28347968383668E-05, 1.49366706221987E-05, 1.41673996772066E-05, 1.12870073587166E-05, 7.44721505240633E-06, 3.73947951133482E-06, 8.97388711088807E-07, -8.02854536477787E-07, -1.47034241750177E-06,
            -1.42785460897851E-06, -1.02623000672594E-06, -5.14389135571994E-07, -1.97995943717345E-21
    };    // BPF Nuttall Window (5-40) of order 200 *
    //----------------------------------------------------------------------------------------------------------------------------------------------------------------

    double FIR_B_25[] = {
            -1.41425674083818E-21, -3.79400868259682E-07, -8.38953152474410E-07, -1.42498634351694E-06, -2.13122927842115E-06, -2.88000700824175E-06, -3.51523021672606E-06, -3.81066611114057E-06, -3.49555124347597E-06, -2.29750663287788E-06,
            1.01472775947401E-20, 3.49151738395260E-06, 8.08380331426253E-06, 1.34419565365520E-05, 1.89605472526916E-05, 2.37724787782665E-05, 2.68057255341330E-05, 2.68930289750196E-05, 2.29322603783641E-05, 1.40860984727828E-05, -5.19580965486729E-20,
            -1.89913759287242E-05, -4.17054101674095E-05, -6.60386280792097E-05, -8.90143559306296E-05, -1.06975746782874E-04, -1.15932816891163E-04, -1.12050732323163E-04, -9.22421002254373E-05, -5.48015328238636E-05, 1.71360906281279E-19,
            6.94565916780677E-05, 1.48201977136063E-04, 2.28302605475681E-04, 2.99727566422805E-04, 3.51212752219255E-04, 3.71484817070988E-04, 3.50753158203152E-04, 2.82322387538684E-04, 1.64131757975184E-04, -4.30807446502761E-19, -1.99648819379914E-04,
            -4.17775586929292E-04, -6.31570914785925E-04, -8.14204792452513E-04, -9.37421417890282E-04, -9.74798302534637E-04, -9.05370770534605E-04, -7.17224384676391E-04, -4.10594709229354E-04, 8.84816398157646E-19, 4.85022677809010E-04,
            1.00090061974109E-03, 1.49288643712911E-03, 1.89974612633154E-03, 2.15998947235217E-03, 2.21912490617621E-03, 2.03721818539231E-03, 1.59589130049825E-03, 9.03840625542575E-04, -1.54221779067633E-18, -1.04636692093915E-03, -2.13909493264683E-03,
            -3.16216167818410E-03, -3.99003222998565E-03, -4.50056143292476E-03, -4.58931878095646E-03, -4.18389159432268E-03, -3.25654274898965E-03, -1.83358452171566E-03, 2.33415265413404E-18, 2.10180173342931E-03, 4.27969183009683E-03,
            6.30594395176792E-03, 7.93702447163816E-03, 8.93760981625029E-03, 9.10678517452754E-03, 8.30393593793532E-03, 6.47162511507316E-03, 3.65279937517378E-03, -3.11174722861151E-18, -4.22514030008926E-03, -8.66300162331462E-03, -1.28775807357347E-02,
            -1.63874413169147E-02, -1.87038475391450E-02, -1.93730574511549E-02, -1.80191392164343E-02, -1.43833839019981E-02, -8.35647022515258E-03, 3.68604637080167E-18, 1.04451606203012E-02, 2.25651959507981E-02, 3.57944821469904E-02,
            4.94503673437435E-02, 6.27791095848699E-02, 7.50093929482694E-02, 8.54090513444043E-02, 9.33402309119614E-02, 9.83082626076671E-02, 1.00000000000000E-01, 9.83082626076671E-02, 9.33402309119614E-02, 8.54090513444043E-02, 7.50093929482694E-02,
            6.27791095848699E-02, 4.94503673437435E-02, 3.57944821469904E-02, 2.25651959507981E-02, 1.04451606203012E-02, 3.68604637080168E-18, -8.35647022515258E-03, -1.43833839019981E-02, -1.80191392164343E-02, -1.93730574511549E-02,
            -1.87038475391450E-02, -1.63874413169147E-02, -1.28775807357347E-02, -8.66300162331462E-03, -4.22514030008926E-03, -3.11174722861151E-18, 3.65279937517378E-03, 6.47162511507316E-03, 8.30393593793533E-03, 9.10678517452755E-03,
            8.93760981625029E-03, 7.93702447163816E-03, 6.30594395176792E-03, 4.27969183009683E-03, 2.10180173342931E-03, 2.33415265413404E-18, -1.83358452171566E-03, -3.25654274898965E-03, -4.18389159432268E-03, -4.58931878095646E-03,
            -4.50056143292476E-03, -3.99003222998565E-03, -3.16216167818410E-03, -2.13909493264683E-03, -1.04636692093915E-03, -1.54221779067633E-18, 9.03840625542576E-04, 1.59589130049825E-03, 2.03721818539231E-03, 2.21912490617621E-03,
            2.15998947235217E-03, 1.89974612633154E-03, 1.49288643712911E-03, 1.00090061974109E-03, 4.85022677809011E-04, 8.84816398157647E-19, -4.10594709229354E-04, -7.17224384676392E-04, -9.05370770534604E-04, -9.74798302534638E-04,
            -9.37421417890283E-04, -8.14204792452514E-04, -6.31570914785926E-04, -4.17775586929292E-04, -1.99648819379914E-04, -4.30807446502762E-19, 1.64131757975185E-04, 2.82322387538685E-04, 3.50753158203153E-04, 3.71484817070989E-04,
            3.51212752219256E-04, 2.99727566422804E-04, 2.28302605475681E-04, 1.48201977136063E-04, 6.94565916780677E-05, 1.71360906281279E-19, -5.48015328238637E-05, -9.22421002254375E-05, -1.12050732323163E-04, -1.15932816891163E-04,
            -1.06975746782874E-04, -8.90143559306296E-05, -6.60386280792094E-05, -4.17054101674094E-05, -1.89913759287243E-05, -5.19580965486730E-20, 1.40860984727828E-05, 2.29322603783642E-05, 2.68930289750198E-05, 2.68057255341331E-05,
            2.37724787782666E-05, 1.89605472526917E-05, 1.34419565365520E-05, 8.08380331426263E-06, 3.49151738395259E-06, 1.01472775947399E-20, -2.29750663287789E-06, -3.49555124347596E-06, -3.81066611114065E-06, -3.51523021672608E-06,
            -2.88000700824166E-06, -2.13122927842135E-06, -1.42498634351687E-06, -8.38953152474410E-07, -3.79400868259682E-07, -1.41425674083818E-21
    };    // LPF Nuttall Window (0-25) of order 200 *

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------

    double FIR_B_5_25[] = {
            -1.13140539267054E-21, -3.02308740442943E-07, -6.60063479760633E-07, -1.09493664595625E-06, -1.57393835018308E-06, -1.99003589877610E-06, -2.15459335767073E-06, -1.80514245901257E-06, -6.30568415159903E-07, 1.68630538376555E-06,
            5.41146514448878E-06, 1.06936356546894E-05, 1.74983712063499E-05, 2.55538956854684E-05, 3.43217297882846E-05, 4.30048181083019E-05, 5.06032835593672E-05, 5.60228755682059E-05, 5.82338088326857E-05, 5.64686320431170E-05, 5.04381001233507E-05,
            4.05352108482690E-05, 2.79912903436689E-05, 1.49459423656209E-05, 4.39618793343820E-06, 0.00000000000000E+00, 5.72562083248263E-06, 2.53594575786596E-05, 6.19098433262185E-05, 1.16968443790521E-04, 1.90111848307753E-04, 2.78438967753643E-04,
            3.76341689077893E-04, 4.75594194701602E-04, 5.65819381196719E-04, 6.35349837405829E-04, 6.72449024916768E-04, 6.66800966935061E-04, 6.11120996511472E-04, 5.02694108646633E-04, 3.44619449744482E-04, 1.46536629825119E-04, -7.53632464219102E-05,
            -2.99180101563988E-04, -4.99051308006712E-04, -6.47742268871126E-04, -7.19900222649554E-04, -6.95672375630877E-04, -5.64290892432302E-04, -3.27164167939394E-04, 7.07853118526117E-19, 3.86468790878849E-04, 7.87478948034623E-04,
            1.14710998859779E-03, 1.40298526963747E-03, 1.49251601772437E-03, 1.36017031258398E-03, 9.65046251093224E-04, 2.87885537367192E-04, -6.63393650798412E-04, -1.85051901048891E-03, -3.20475752607462E-03, -4.63033002188907E-03,
            -6.01144256382409E-03, -7.22261895814603E-03, -8.14159211638936E-03, -8.66361924512557E-03, -8.71577680585276E-03, -8.26961166370904E-03, -7.35051014138342E-03, -6.04231656367479E-03, -4.48608761922065E-03, -2.87238744606039E-03,
            -1.42716887987002E-03, -3.91989031935612E-04, 0.00000000000000E+00, -4.49760476027846E-04, -1.87935684834830E-03, -4.34354048705528E-03, -7.79653846118358E-03, -1.20828612825136E-02, -1.69378265669009E-02, -2.19986853509352E-02,
            -2.68262494287171E-02, -3.09358662467983E-02, -3.38355780585113E-02, -3.50684415460140E-02, -3.42553706840661E-02, -3.11345762552809E-02, -2.55937571320009E-02, -1.76916617716381E-02, -7.66645472803114E-03, 4.07057395454401E-03,
            1.69561272589584E-02, 3.03096600917476E-02, 4.33792978314179E-02, 5.53953351620124E-02, 6.56269448748128E-02, 7.34373277407640E-02, 7.83325752004245E-02, 8.00000000000000E-02, 7.83325752004245E-02, 7.34373277407640E-02, 6.56269448748128E-02,
            5.53953351620124E-02, 4.33792978314179E-02, 3.03096600917476E-02, 1.69561272589584E-02, 4.07057395454401E-03, -7.66645472803114E-03, -1.76916617716381E-02, -2.55937571320009E-02, -3.11345762552809E-02, -3.42553706840661E-02,
            -3.50684415460140E-02, -3.38355780585113E-02, -3.09358662467983E-02, -2.68262494287171E-02, -2.19986853509352E-02, -1.69378265669009E-02, -1.20828612825136E-02, -7.79653846118358E-03, -4.34354048705529E-03, -1.87935684834830E-03,
            -4.49760476027847E-04, 0.00000000000000E+00, -3.91989031935612E-04, -1.42716887987002E-03, -2.87238744606039E-03, -4.48608761922065E-03, -6.04231656367479E-03, -7.35051014138343E-03, -8.26961166370904E-03, -8.71577680585275E-03,
            -8.66361924512556E-03, -8.14159211638936E-03, -7.22261895814603E-03, -6.01144256382409E-03, -4.63033002188907E-03, -3.20475752607462E-03, -1.85051901048891E-03, -6.63393650798413E-04, 2.87885537367192E-04, 9.65046251093224E-04,
            1.36017031258398E-03, 1.49251601772437E-03, 1.40298526963747E-03, 1.14710998859779E-03, 7.87478948034623E-04, 3.86468790878850E-04, 7.07853118526117E-19, -3.27164167939394E-04, -5.64290892432303E-04, -6.95672375630877E-04,
            -7.19900222649554E-04, -6.47742268871126E-04, -4.99051308006712E-04, -2.99180101563988E-04, -7.53632464219102E-05, 1.46536629825119E-04, 3.44619449744482E-04, 5.02694108646634E-04, 6.11120996511473E-04, 6.66800966935062E-04,
            6.72449024916770E-04, 6.35349837405831E-04, 5.65819381196718E-04, 4.75594194701601E-04, 3.76341689077893E-04, 2.78438967753643E-04, 1.90111848307753E-04, 1.16968443790521E-04, 6.19098433262186E-05, 2.53594575786598E-05, 5.72562083248265E-06,
            0.00000000000000E+00, 4.39618793343820E-06, 1.49459423656208E-05, 2.79912903436688E-05, 4.05352108482692E-05, 5.04381001233507E-05, 5.64686320431169E-05, 5.82338088326859E-05, 5.60228755682062E-05, 5.06032835593673E-05, 4.30048181083020E-05,
            3.43217297882847E-05, 2.55538956854684E-05, 1.74983712063501E-05, 1.06936356546894E-05, 5.41146514448864E-06, 1.68630538376555E-06, -6.30568415159902E-07, -1.80514245901261E-06, -2.15459335767074E-06, -1.99003589877604E-06,
            -1.57393835018323E-06, -1.09493664595620E-06, -6.60063479760633E-07, -3.02308740442943E-07, -1.13140539267054E-21
    };    // BPF Nuttall Window (5-25) of order 200 *

    InputStream InStream;
    OutputStream OutStream;
    boolean START = false,
            array_full = false,
            plot_done = false;
    Paint paint = new Paint();
    Path path = new Path();
    public Canvas cnvs;
    public Bitmap bitmap;
    static float singleLeadHeight;
    public static int page, filter_state = 3;
    public static boolean Auto;//			=true;
    private Object mPauseLock;
    private boolean mPaused;
    char byte1[] = new char[8],
            byte2[] = new char[8],
            BATT_adc[] = new char[3],
            status1, status2, status3;
    int adj_height;
    public static String Pass_On_date, Pass_On_chno, Pass_On_name, Pass_On_age, Pass_On_gen, Pass_On_medi, Pass_On_BP, Pass_On_dob, Pass_On_ht, Pass_On_wt, Pass_On_comment;
    int lead_arrange[] = {1, 2, 8, 9, 10, 11, 7, 3, 5, 4, 6, 0};
    public static float CAL_DATA[][] = new float[CH_NUM][25];
    public ArrayList<Float> List_arrayOf_v1 = new ArrayList<Float>();//used for load data
    @SuppressWarnings("unchecked")
    public ArrayList<Float> List_arrayOf_0[] = new ArrayList[CH_NUM],
            List_arrayOf_rpt_data[] = new ArrayList[CH_NUM];
    public static float Difference[] = new float[CH_NUM];
    public static float Max[] = new float[CH_NUM], Min[] = new float[CH_NUM];
    public static float Max_for_report[] = new float[CH_NUM], Min_for_report[] = new float[CH_NUM];
    public float current_y1;
    public float current_y[] = new float[CH_NUM];
    boolean init_done = false, first_pass = true, debug_mode = false;
    public int report_count = 0;//, selected_lead;
    public static int skip_point;
    public int disp_delay_points;
    public static int Lead_I = 1,
            Lead_II = 2,
            Lead_III = 8,
            aVR = 9,
            aVL = 10,
            aVF = 11,
            V1 = 7,
            V2 = 3,
            V3 = 5,
            V4 = 4,
            V5 = 6,
            V6 = 0;


    PaintView(int title_height1, int status_height, int width, InputStream in, OutputStream out, Context context) {
        super(context);
        //initialise variables
        //selected_lead=MainActivity.no_of_lead_to_display;//SNB
        iScreenHeight_bmp = (MainActivity.iScreenHeight - title_height1);//(title_height1-status_height);
        iStatus_Height = status_height;
        iScreenWidth = width;
        MainActivity.step = ((XAXIS_WD / iScreenWidth));
        skip_point = (MainActivity.step + 2);
        System.out.println("skip_point=" + skip_point);
        disp_delay_points = 5;
        InStream = in;
        OutStream = out;
        bitmap = Bitmap.createBitmap(iScreenWidth, iScreenHeight_bmp, Bitmap.Config.ARGB_8888);
        cnvs = new Canvas(bitmap);
        cnvs.drawRGB(0, 0, 0);
        setDrawingCacheEnabled(true);
        mPauseLock = new Object();
        mPaused = false;
        //height=(iScreenHeight_bmp/selected_lead);
        singleLeadHeight = (iScreenHeight_bmp / MainActivity.no_of_lead_to_display);//for each lead Graph
        start_position = (singleLeadHeight / 2);
        if (MainActivity.load_existing_file == true)
            page = 1;
        else
            page = 0;
    }

    //to copy  the patient details from patient info to paintview
    public PaintView(String date1, String sname, String schno, String sdob, String sage, String sgen, String sht, String swt, String smedi, String sBP, String sComments, Context c) {
        //"2/4/12","Akash","E256","7/9/1990","50","Male","150","75","no","80"
        super(c);
        Pass_On_date = date1;
        Pass_On_name = sname;
        Pass_On_chno = schno;
        Pass_On_dob = sdob;
        Pass_On_age = sage;
        Pass_On_gen = sgen;
        Pass_On_ht = sht;
        Pass_On_wt = swt;
        Pass_On_medi = smedi;
        Pass_On_BP = sBP;
        Pass_On_comment = sComments;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap != null)
            canvas.drawBitmap(bitmap, 0, 0, paint);
        if ((Auto == false && MainActivity.refresh_data == true))//should be changed for 12 Channels and manual mode
            refresh_canvas();
        else if ((array_full == true && Auto == true) || MainActivity.refresh_data == true)//Auto mode
            refresh_canvas();
        if (MainActivity.load_existing_file == true && START_GUI == true) {
            store_avg_array(page);
            load_data(MainActivity.lead_page);
        }
    }

    private void refresh_canvas() {
        try {
            bitmap.eraseColor(Color.TRANSPARENT);
            cnvs.drawRGB(0, 0, 0);
            drawGridLines(cnvs);
            array_full = false;
            MainActivity.refresh_data = false;
            print_text_on_canvas(page);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void load_data(int lead_pg) {
        try {
            if (plot_on == true) {
                acquired_pts = index;

                Max_Scaling_factor = Collections.max(List_arrayOf_v1);
                Min_Scaling_factor = Collections.min(List_arrayOf_v1);

                Max_Scaling_factor = Max_Scaling_factor + 20;
                Min_Scaling_factor = Min_Scaling_factor - 20;

                if ((Max_Scaling_factor - Min_Scaling_factor) <= 30 * MainActivity.iGain)
                    Max_Scaling_factor = Min_Scaling_factor + 30 * MainActivity.iGain;

                paint.setColor(Color.YELLOW);
                set_CAL(MainActivity.iGain);
                cal_data_position();

                if (acquired_pts == XAXIS_WD)//draws straight line at the end of data position
                    last_position(acquired_pts);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("load error.." + e.getMessage());
        }
    }

    //calculates average and stores the array for displaying on screen
    public void store_avg_array(int page) {
        try {
            int avg_incmt = MainActivity.step;
            for (int i = 0; i < CH_NUM; i++) {
                ADS_samples_count = 0;
                disp_count = 0;
                Max_Scaling_factor = 0;
                Min_Scaling_factor = 0;
                for (int j = 0; j < XAXIS_WD; j++) {
                    DC[i] += raw_data[i][ADS_samples_count];
                    ADS_samples_count++;

                    if (ADS_samples_count % XAXIS_WD == 0) {
                        AVE[i] = (short) (DC[i] / XAXIS_WD);
                        DC[i] = 0;

                        if (iScreenWidth <= XAXIS_WD)    //for small size mobile
                        {
                            for (int l = (ADS_samples_count - (XAXIS_WD - avg_incmt)); l <= ADS_samples_count; l += avg_incmt) {
                                int sum = 0;
                                for (int k = l - avg_incmt; k < l; k++)
                                    sum = sum + Gen_report[i][k + (XAXIS_WD * page)];
                                display_load_data[i][disp_count] = (float) (sum / avg_incmt) - AVE[i];
                                List_arrayOf_v1.add((float) display_load_data[i][disp_count]);
                                disp_count++;
                            }
                        } else {
                            for (int l = 0; l < ADS_samples_count; l++) {
                                display_load_data[i][disp_count] = (short) ((Gen_report[i][l + (XAXIS_WD * page)]) - AVE[i]);
                                List_arrayOf_v1.add((float) display_load_data[i][disp_count]);
                                disp_count++;
                            }
                        }
                    }//if(ADS_samples_count%50==0)
                    index = disp_count;
                    plot_on = true;
                }//for(int j=0;j<XAXIS_WD;j++)
            }//for(int i=0;i<ch_num;i++)
            report_data();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("store avg data=" + e.getMessage());
        }
    }


    //sets and plots the calibration according to gain value
    public void set_CAL(int Gain) {
        //for (int j = 0; j < selected_lead; j++)
        for (int j = 0; j < MainActivity.no_of_lead_to_display; j++) {
            for (int i = 0; i < cal_width; i++) {
                if ((i < 5) || (i >= cal_width - 5))//for start and end
                {
                    for (int a = 0; a < CH_NUM; a++)
                        CAL_DATA[a][i] = (a * singleLeadHeight) + start_position;
                } else {
                    for (int a = 0; a < CH_NUM; a++)
                        CAL_DATA[a][i] = (int) ((a * singleLeadHeight) + start_position) - adjust_Height_function(27 * Gain);
                }
            }
        }
        //for (int j = 0; j < selected_lead; j++) {
        //draws calibration on screen
        for (int i = 0; i < cal_width - 1; i++)
            //for (int a = 0;  a<selected_lead; a++)
            for (int a = 0; a < MainActivity.no_of_lead_to_display; a++)
                cnvs.drawLine(i, CAL_DATA[a][i], i + 1, CAL_DATA[a][i + 1], paint);
        //}
    }

    //calculates the data position to display data on mobile screen
    private void cal_data_position() {
        try {
            //called during load data
            if (MainActivity.load_existing_file == true) {
                for (int pts = 0; pts < acquired_pts; pts++) {
                    for (int lead_no = 0; lead_no < MainActivity.no_of_lead_to_display; lead_no++)
                    //for (int lead_no = 0; lead_no< selected_lead; lead_no++)
                    {
                        //adjust_Height=adjust_Height_function(display_load_data[lead_arrange[MainActivity.lead_page*selected_lead+lead_no]][pts]);
                        adjust_Height = adjust_Height_function(display_load_data[lead_arrange[MainActivity.lead_page * MainActivity.no_of_lead_to_display + lead_no]][pts]);
                        current_y1 = ((lead_no * singleLeadHeight) + start_position) - adjust_Height;
                        display_data[lead_arrange[MainActivity.lead_page * MainActivity.no_of_lead_to_display + lead_no]][pts] = current_y1;

                        if (pts == 0)
                            cnvs.drawPoint(pts + cal_width, display_data[lead_arrange[MainActivity.lead_page * MainActivity.no_of_lead_to_display + lead_no]][pts], paint);
                            //cnvs.drawPoint(pts+cal_width, display_data[lead_arrange[MainActivity.lead_page*selected_lead+lead_no]][pts], paint);
                        else
                            cnvs.drawLine(pts + cal_width, display_data[lead_arrange[MainActivity.lead_page * MainActivity.no_of_lead_to_display + lead_no]][pts], pts + cal_width - 1, display_data[lead_arrange[MainActivity.lead_page * MainActivity.no_of_lead_to_display + lead_no]][pts - 1], paint);
                        //cnvs.drawLine(pts+cal_width, display_data[lead_arrange[MainActivity.lead_page*selected_lead+lead_no]][pts],pts+cal_width-1,display_data[lead_arrange[MainActivity.lead_page*selected_lead+lead_no]][pts-1], paint);
                    }
                }
            } else //called during data acquisition
            {
                //for (int lead_no = 0; lead_no<selected_lead; lead_no++)
                for (int lead_no = 0; lead_no < MainActivity.no_of_lead_to_display; lead_no++) {
                    adjust_Height = adjust_Height_function((raw_data[lead_arrange[page * MainActivity.no_of_lead_to_display + lead_no]][fill_count] - AVE[lead_arrange[page * MainActivity.no_of_lead_to_display + lead_no]]));
                    //adjust_Height = adjust_Height_function((raw_data[lead_arrange[page*selected_lead+lead_no]][fill_count]-AVE[lead_arrange[page*selected_lead+lead_no]]));
                    current_y1 = ((lead_no * singleLeadHeight) + start_position - adjust_Height);

                    display_data[lead_arrange[page * MainActivity.no_of_lead_to_display + lead_no]][disp_count] = current_y1;    //..I
                    //display_data[lead_arrange[page*selected_lead+lead_no]][disp_count]=current_y1;	//..I
                }
            }//if(MainActivity.load_existing_file==true)
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Plot_graph(int pg) {
        try {
            postInvalidate();
            change_color_of_previous_points();
            plot_current_point(pg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //plots the current ecg data on screen
    private void plot_current_point(int pg) {
        try {
            i = disp_count;
            //plot new points in yellow color
            paint.setColor(Color.YELLOW);
            set_CAL(MainActivity.iGain);
            if (i == 0) {
                //draw calibration
                for (int lead_no = 0; lead_no < MainActivity.no_of_lead_to_display; lead_no++)
                    //for (int lead_no = 0; lead_no< selected_lead; lead_no++)
                    //cnvs.drawPoint(i+cal_width, display_data[lead_arrange[pg*selected_lead+lead_no]][i], paint);
                    cnvs.drawPoint(i + cal_width, display_data[lead_arrange[pg * MainActivity.no_of_lead_to_display + lead_no]][i], paint);
            } else {
                for (int p = disp_count - disp_delay_points; p < disp_count; p++) {
                    if (p >= 0) {
                        if (p == 0) {
                            //draw calibration
                            //for (int lead_no = 0; lead_no<selected_lead; lead_no++)
                            for (int lead_no = 0; lead_no < MainActivity.no_of_lead_to_display; lead_no++)
                                //cnvs.drawPoint(i+cal_width, display_data[lead_arrange[pg*selected_lead+lead_no]][i], paint);
                                cnvs.drawPoint(i + cal_width, display_data[lead_arrange[pg * MainActivity.no_of_lead_to_display + lead_no]][i], paint);
                            //System.out.println("i 0=="+i);
                        } else {
                            //for (int lead_no = 0; lead_no<selected_lead; lead_no++)
                            for (int lead_no = 0; lead_no < MainActivity.no_of_lead_to_display; lead_no++) {
                                //System.out.println("i current=="+i);
                                //cnvs.drawLine(p+cal_width, display_data[lead_arrange[pg*selected_lead+lead_no]][p],p+cal_width-1,display_data[lead_arrange[pg*selected_lead+lead_no]][p-1], paint);
                                cnvs.drawLine(p + cal_width, display_data[lead_arrange[pg * MainActivity.no_of_lead_to_display + lead_no]][p], p + cal_width - 1, display_data[lead_arrange[pg * MainActivity.no_of_lead_to_display + lead_no]][p - 1], paint);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void change_color_of_previous_points() {
        try {
            //change the color to black of the previous points
            if (array_full == true) {
                i = disp_count;
                if (i == 0) {
                    for (int lead_no = 0; lead_no < CH_NUM; lead_no++) {
                        paint.setColor(Color.BLACK);
                        cnvs.drawPoint(i + cal_width, pre_display_data[lead_no][i], paint);
                    }
                } else {

                    for (int p = disp_count - disp_delay_points; p < disp_count; p++) {
                        if (p >= 0) {
                            //System.out.println("disp_count="+disp_count);
                            //System.out.println("disp_delay_points="+disp_delay_points);
                            for (int lead_no = 0; lead_no < CH_NUM; lead_no++) {
                                if (p == 0) {
                                    paint.setColor(Color.BLACK);
                                    cnvs.drawPoint(i + cal_width, pre_display_data[lead_no][i], paint);
                                } else if ((pre_display_data[lead_no][p] != display_data[lead_no][p]) || (pre_display_data[lead_no][p - 1] != display_data[lead_no][p - 1])) {
                                    paint.setColor(Color.BLACK);
                                    cnvs.drawLine(p + cal_width, pre_display_data[lead_no][p], p + cal_width - 1, pre_display_data[lead_no][p - 1], paint);
                                }
                                //System.out.println("p=="+p);
                                /*if((pre_display_data[lead_no][p]!=display_data[lead_no][p]) || (pre_display_data[lead_no][p-1]!=display_data[lead_no][p-1]))
                                {
									paint.setColor(Color.BLACK);
									cnvs.drawLine(p+cal_width, pre_display_data[lead_no][p], p+cal_width-1,pre_display_data[lead_no][p-1], paint);
									//cnvs.drawLine(p+cal_width, display_data[lead_arrange[pg*total_lead+lead_no]][p],p+cal_width-1,display_data[lead_arrange[pg*total_lead+lead_no]][p-1], paint);
								}*/
                            }
                        }


                    }
                }

            }//if(array_full==true)
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void last_position(int a) {
        if (MainActivity.stop == true || MainActivity.load_existing_file == true) {
            paint.setColor(Color.CYAN);
            cnvs.drawLine(a + cal_width, 0, a + cal_width, iScreenHeight_bmp, paint);
        }
    }

    //it takes  one sample data point and scales with respect  to height of mobile screen
    private float adjust_Height_function(float display_data2) {
        float value;
        float result = singleLeadHeight / (Max_Scaling_factor - Min_Scaling_factor);
        value = result * (display_data2);
        return value;
    }

    public final Handler p_handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_COMMUNICATION:
                    Toast.makeText(getContext(), "Communication Problem, Restart acquisition...", Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().post(new MessageEvent("", "", MessageEvent.BLUETOOTH_DISCONNECTED));
                    communication_error = true;
                    onPause();
                    last_position(disp_count);
                    if (report_count > 0) {
                        store_Array(ADS_samples_count);
                        generate_report_opt = true;
                    }
                    //else
                    //    Toast.makeText(getContext(), "sufficient data not collected", Toast.LENGTH_SHORT).show();

                    if (generate_report_opt == true)
                        MainActivity.gen_report_menu.setVisible(true);
                    break;

            }
        }
    };

    public void onPause() {
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }

    public void onResume() {
        synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notifyAll();
        }
    }

    Thread echo = new Thread() {
        int call_plot_graph_count = 0;

        public void run() {
            while (true) {
                try {
                    s = InStream.read();
                    if (s == 0xAA)    // Start of Packet
                    {
                        //read three status bytes used for finding lead connection at the start of the page
                        status1 = (char) InStream.read();
                        status2 = (char) InStream.read();
                        status3 = (char) InStream.read();
                        check_Lead_status(status1, status2, status3);
                        System.out.println("status3 thread=" + status3);
                        //Battery_Status(status3);
                    }
                    if (s == 0xAA || s == 0xBB) {
                        for (int i = 0; i < 8; i++) {
                            //read data of two bytes each for all channels
                            byte1[i] = (char) InStream.read();
                            byte2[i] = (char) InStream.read();

                            if (debug_mode == true)
                                temp_ADS_data[i][temp_index] = (short) (((byte1[i] << 8) | (byte2[i])));
                            else
                                temp_ADS_data[i][temp_index] = (short) (((byte1[i] << 8) | (byte2[i])) << 1);//... S/W Gain of 2
                        }//...ACQ ALL 8-Channels

                        e = InStream.read();

                        if (e == 0x0A) // End of Packet
                        {
                            OutStream.write('y');
                            //called if filter is ON , roll_over to check if it is start of data acquisition
                            for (int i = 0; i < 8; i++) {
                                if (filter_state != 0) {
                                    if ((ADS_samples_count <= ORDER) && (roll_over == false) && (init_done == false))
                                        raw_data[i][ADS_samples_count] = AVE[i];
                                    else
                                        raw_data[i][ADS_samples_count] = (short) Filter_ECG(i);
                                } else
                                    raw_data[i][ADS_samples_count] = temp_ADS_data[i][temp_index];
                            }//for(int i=0;i<8;i++)

                            temp_index++;
                            if (temp_index == ORDER + 1)
                                temp_index = 0;

                            //calculate remaining chs.
                            raw_data[8][ADS_samples_count] = (short) (raw_data[2][ADS_samples_count] - raw_data[1][ADS_samples_count]);
                            raw_data[9][ADS_samples_count] = (short) ((-1) * (raw_data[1][ADS_samples_count] + raw_data[2][ADS_samples_count]) / 2.0);
                            raw_data[10][ADS_samples_count] = (short) (raw_data[1][ADS_samples_count] - (raw_data[2][ADS_samples_count] / 2.0));
                            raw_data[11][ADS_samples_count] = (short) (raw_data[2][ADS_samples_count] - (raw_data[1][ADS_samples_count] / 2.0));

                            if (init_done == false) {
                                for (int i = 0; i < CH_NUM; i++)
                                    DC[i] += raw_data[i][ADS_samples_count];

                                if (ADS_samples_count == (XAXIS_WD - 1)) {
                                    for (int i = 0; i < CH_NUM; i++) {
                                        if (filter_state == 0)
                                            AVE[i] = (short) (DC[i] / XAXIS_WD);
                                        else
                                            AVE[i] = (short) (DC[i] / (XAXIS_WD - ORDER));

                                        if (debug_mode == true)
                                            AVE[i] = 0;
                                    }//for

                                    Store_cal_min_max();
                                    init_done = true;
                                    //reset raw data back to zero after Min Max calculation
                                    for (int i = 0; i < CH_NUM; i++) {
                                        for (int j = 0; j < XAXIS_WD; j++)
                                            raw_data[i][j] = 0;
                                    }

                                    ADS_samples_count = 0;
                                    disp_count = 0;
                                }//if(ADS_samples_count==(XAXIS_WD-1))
                                else
                                    ADS_samples_count++;
                            }//if(init_done==false)
                            else {
                                try {
                                    if (ADS_samples_count % skip_point == 0) {
                                        cal_data_position();//used disp_delay_points in place of skip_point
                                        if (call_plot_graph_count == disp_delay_points) {
                                            Plot_graph(page);    //current page being displayed, display count
                                            call_plot_graph_count = 0;
                                        }
                                        //increment fill_count depending on phone size
                                        fill_count += skip_point;
                                        disp_count++;
                                        call_plot_graph_count++;

                                        //init fill_count after total samples are acquired
                                        if (fill_count >= (total_samples))
                                            fill_count = 0;

                                        if (disp_count == iScreenWidth) {
                                            START = false;
                                            disp_count = 0;
                                            array_full = true;
                                            if (Auto == true) {
                                                //if auto option is on
                                                page++;
                                                START = false;
                                                if (page > (MainActivity.total_pages - 1)) {
                                                    page = 0;
                                                }//if(page>(MainActivity.total_pages-1))
                                            }//if(Auto==true && START==true)

                                            //copy data from display data array to pre_display_data array
                                            for (int j = 0; j < CH_NUM; j++) {
                                                DC[j] = 0;
                                                int m = fill_count;
                                                for (int k = 0; k < iScreenWidth; k++) {
                                                    pre_display_data[j][k] = display_data[j][k];

                                                    DC[j] = DC[j] + raw_data[j][m];
                                                    if (m == 0)
                                                        m = total_samples - 1;
                                                    else
                                                        m--;
                                                }
                                                AVE[j] = (short) (DC[j] / iScreenWidth);
                                                if (debug_mode == true)
                                                    AVE[j] = 0;
                                            }//for (int j = 0; j < ch_num; j++)
                                        }//if(disp_count==iScreenWidth)
                                    }//if(ADS_samples_count%skip_point==0)

                                    ADS_samples_count++;

                                    if (ADS_samples_count == total_samples) {
                                        Log.d("SampleReading", ADS_samples_count + " : Report count :" + report_count);
                                        ADS_samples_count = 0;
                                        roll_over = true;
                                        report_count++;
                                        if (report_count == 1) {
                                            EventBus.getDefault().post(new MessageEvent("Sufficient Data acquired for report generation", MessageEvent.TOAST_MESSAGE));
                                        }
                                    }//if(ADS_samples_count==total_samples)

                                } catch (Exception e) {
                                    Message report = p_handler.obtainMessage(MESSAGE_COMMUNICATION);
                                    p_handler.sendMessage(report);
                                    e.printStackTrace();
                                }
                            }//else
                        }//end of packet
                        else if (e != 0x0A) {
                            OutStream.write('x');
                            System.out.println("ERROR...eof");
                        }
                    }//if start of packet
                    else if (s != 0xAA) {
                        System.out.println("ERROR...sof ");
                    }
                }//try
                catch (Exception e) {
                    Message report = p_handler.obtainMessage(MESSAGE_COMMUNICATION);
                    p_handler.sendMessage(report);
                    e.printStackTrace();
                    System.out.println("inside thread run" + e.getMessage());
                    break;
                }

                synchronized (mPauseLock) {
                    while (mPaused) {
                        try {
                            mPauseLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }//while (mPaused)
                }//synchronised
            }//while
        }//run
    };//thread

    //function is used for checking leads are properly connected or not
    private void check_Lead_status(char byte1, char byte2, char byte3) {
        if ((byte1 & 0x08) != 0) {
            slead_names = " V1";
        }

        if ((byte1 & 0x04) != 0) {
            slead_names = " V5";
        }

        if ((byte1 & 0x02) != 0) {
            slead_names = " V3";    // V3
        }

        if ((byte1 & 0x01) != 0) {
            slead_names = " V4";    // V4
        }

        if ((byte2 & 0x80) != 0) {
            slead_names = " V2";        // V2
        }

        if ((byte2 & 0x40) != 0) {
            slead_names = " LL";        // LL
        }

        if ((byte2 & 0x20) != 0) {
            slead_names = " LA";        // LA
        }

        if ((byte2 & 0x10) != 0) {
            slead_names = " V6";        // V6
        }

        if ((byte3 & 0x20) != 0) {
            slead_names = " RA";        // RA (opposite of above)
        }
    }

    @SuppressWarnings("unused")
    private void Battery_Status(char byte3) {
        try {
            BATT_adc[BATT_index] = (char) ((byte3 & 0x0F) * 7);
            System.out.println("status3 byte3=" + byte3);
            if (batt_count <= 2) {
                switch (batt_count) {
                    case 0:
                        //Battery_level=BATT_adc[0];
                        BATT_adc[1] = BATT_adc[0];
                        BATT_adc[2] = BATT_adc[0];
                        Battery_level = (BATT_adc[0] + BATT_adc[1] + BATT_adc[2]) / 3;
                        System.out.println("battery 0=" + Battery_level);
                        break;
                    case 1:
                        Battery_level = (BATT_adc[0] + BATT_adc[1]) / 2;
                        System.out.println("battery 1=" + Battery_level);
                        break;
                    default:
                        Battery_level = (BATT_adc[0] + BATT_adc[1] + BATT_adc[2]) / 3;
                        System.out.println("battery 2=" + Battery_level);
                        break;
                }
            }

            System.out.println("BATT_adc[0]:" + BATT_adc[0] + " BATT_adc[1]:" + BATT_adc[1] + "BATT_adc[2]:" + BATT_adc[2]);
            System.out.println("data==" + BATT_adc[BATT_index] + " index=" + BATT_index);
            BATT_index++;
            batt_count++;

            if (BATT_index == 3) {
                BATT_index = 0;
            }
            if (Battery_level > 100)
                Battery_level = 100;
            paint.setColor(Color.WHITE);
            if (Battery_level != 0)
                cnvs.drawText("BATT: " + Battery_level + "%", iScreenWidth / 2, iScreenHeight_bmp, paint);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //initialise the array values to  0
    public void initialise_array() {
        //for each channel
        for (int i = 0; i < CH_NUM; i++) {
            DC[i] = 0;
            AVE[i] = 0;
            //for all the samples
            for (int j = 0; j < total_samples; j++) {
                raw_data[i][j] = 0;
            }
            //for the given width of the screen
            for (int k = 0; k < iScreenWidth; k++) {
                display_data[i][k] = 0;
                pre_display_data[i][k] = 0;
            }
            //x axis width: total_samples/5;//1300
            for (int j = 0; j < XAXIS_WD; j++) {
                display_data_report[i][j] = 0;
                display_load_data[i][j] = 0;
            }

            Max[i] = 0;
            Min[i] = 0;

            List_arrayOf_0[i] = new ArrayList<Float>();

        }//for(int i=0;i<ch_num;i++)
        for (int j = 0; j < 3; j++) {
            BATT_adc[j] = 0;
        }
    }

    //storing the recent data in an raw_data[][] array after data ACQ is stopped
    public void store_Array(int samples) {
        try {
            //copy data from raw_data array to Gen_report used for creating report
            plot_pt = samples;
            //System.out.println("plot_pt="+plot_pt);
            for (int i = 0; i < CH_NUM; i++) {
                for (int j = 0; j < total_samples; j++) {
                    Gen_report[i][(total_samples - 1) - j] = raw_data[i][plot_pt];//eg. plot_pt=2876

                    if (plot_pt == 0)
                        plot_pt = total_samples - 1;
                    else
                        plot_pt--;
                }
            }
            //stores data in array
            report_data();

        }//try
        catch (Exception e) {
            e.printStackTrace();
            //System.out.println("Error in store Array"+e);
        }
    }

    //stores the data values in arraylist for calculating min max values
    private void Store_cal_min_max() {
        for (int j = ADS_samples_count; j > 300; j--) {
            for (int i = 0; i < CH_NUM; i++)
                List_arrayOf_0[i].add((float) raw_data[i][j]);
        }

        for (int i = 0; i < CH_NUM; i++) {

            Max[i] = Collections.max(List_arrayOf_0[i]);
            Min[i] = Collections.min(List_arrayOf_0[i]);
        }


        float Diff = 0;
        //find out the min max of all channels and check which channel has maximum and min value
        for (int i = 0; i < CH_NUM; i++) {
            if ((Max[i] - Min[i]) > Diff) {
                Diff = (Max[i] - Min[i]);
                Max_Scaling_factor = Max[i];
                Min_Scaling_factor = Min[i];
            }
        }
        Max_Scaling_factor = Max_Scaling_factor + 20;
        Min_Scaling_factor = Min_Scaling_factor - 20;

        max_value = Max_Scaling_factor * singleLeadHeight / (Max_Scaling_factor - Min_Scaling_factor);
        min_value = Min_Scaling_factor * singleLeadHeight / (Max_Scaling_factor - Min_Scaling_factor);

    }

    //used to calculate/store data to print on ecg report in create_report.java class
    public void report_data() {
        try {
            for (int i = 0; i < CH_NUM; i++) {
                for (int j = 0; j < total_samples; j++) {
                    if (j % XAXIS_WD == 0 && j != 0) {
                        for (int k = j - XAXIS_WD; k < j; k++) {
                            disp_report[i][k] = ((Gen_report[i][k] - AVE[i]) / MainActivity.iGain);

                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("report data error=" + e.getMessage());
        }
    }

    //filters the data in channels if filter is ON
    int Filter_ECG(int Ch) {
        int i;
        FIR_data = 0;
        FIR_count = temp_index;
        for (i = 0; i <= ORDER; i++) {

            FIR_data = (float) (FIR_data + FIR_B[i] * temp_ADS_data[Ch][FIR_count]);
            if (FIR_count == 0)
                FIR_count = ORDER;
            else
                FIR_count--;
        }
        return ((int) (FIR_data));

    } //Filter_ECG

    //divides the screen into 3 equal parts
    public void drawGridLines(Canvas canvas) {
        paint.setColor(Color.GRAY);
        //for (int i = 1; i <=selected_lead; i++)
        for (int i = 1; i <= MainActivity.no_of_lead_to_display; i++) {
            canvas.drawLine(0, i * singleLeadHeight, iScreenWidth, i * singleLeadHeight, paint);
            //paint.setColor(Color.GRAY);
        }
    }

    //plot the lead name
    public void print_text_on_canvas(int pg) {
        paint.setTextSize(MainActivity.f_size);
        paint.setColor(Color.WHITE);
        //prints filter status on screen
        print_selected_filter(pg);
        //print lead names on screen
        print_lead_name(pg);

    }//public  void plot_lead_name(int page)

    private void print_lead_name(int pg) {
        try {
            //int x_value=iScreenWidth-45;	//45 is the width of lead name RKJ:shd be different for diff screen size.
            int x_value = 10;    //45 is the width of lead name RKJ:shd be different for diff screen size.
            int y_value;
            y_value = (int) (singleLeadHeight);
            paint.setColor(Color.WHITE);
            final String lead_names[] = {"I", "II", "III", "aVR", "aVL", "aVF", "V1", "V2", "V3", "V4", "V5", "V6"};
            //printing lead names on screen

            if (MainActivity.load_existing_file == true)//print lead name during load data
            {
                //for(int i=0;i<selected_lead;i++)
                for (int i = 0; i < MainActivity.no_of_lead_to_display; i++) {
                    //cnvs.drawText(lead_names[MainActivity.lead_page*selected_lead+i], x_value,(y_value*(i+1)-3),paint);
                    cnvs.drawText(lead_names[MainActivity.lead_page * MainActivity.no_of_lead_to_display + i], x_value, (y_value * (i + 1) - 3), paint);
                    paint.setColor(Color.WHITE);
                }
            } else {
                //print lead name during data Acquisition
                //for(int i=0;i<selected_lead;i++)
                for (int i = 0; i < MainActivity.no_of_lead_to_display; i++) {
                    //cnvs.drawText(lead_names[pg*selected_lead+i], x_value,(y_value*(i+1)-3),paint);
                    cnvs.drawText(lead_names[pg * MainActivity.no_of_lead_to_display + i], x_value, (y_value * (i + 1) - 3), paint);
                    paint.setColor(Color.WHITE);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String print_selected_filter(int pg) {

        try {

            String filter_txt_arr[] = {"Filter:off", "Filter:50Hz Notch", "Filter:5 to 40 Hz", "Filter:0 to 40 Hz", "Filter:5 to 25 Hz", "Filter:0 to 25 Hz"};


            if (MainActivity.load_existing_file == true) {
                // cnvs.drawText("", (iScreenWidth / 2), 15, paint);
                for (int i = 0; i < 6; i++) {
                    if (filter_state == i) {
                        cnvs.drawText(filter_txt_arr[i], ((iScreenWidth) / 3) * 2, (singleLeadHeight / 5), paint);
                    }
                }

            //print patient Name
                cnvs.drawText("name:" + PatientInfo.sname, 50, (singleLeadHeight / 5), paint);//3/4,6/7


                //print page number
                cnvs.drawText("pg:" + pg, ((iScreenWidth) / 2) * 1, (singleLeadHeight / 5), paint);//3/4,6/7`

            }//if(MainActivity.load_existing_file==true)
            //prints details about filter on canvas
            if (MainActivity.mode == 0) //ECG Mode
            {
                for (int i = 0; i < 6; i++) {
                    if (filter_state == i) {
                        //text=filter_txt_arr[i];
                        txt_filter = filter_txt_arr[i];    //used in report making...
                        break;
                    }
                }
            } else if (MainActivity.mode == 1) {
                txt_filter = "No Filter";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return txt_filter;

    }
}
