package com.chariotsolutions.nfc.plugin;

import java.io.IOException;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.IsoDep; //Gleason
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {

  static final String TAG = "NfcPlugin";

  static JSONObject ndefToJSON(Ndef ndef) {
    JSONObject json = new JSONObject();

    if (ndef != null) {
      try {

        Tag tag = ndef.getTag();
        // tag is going to be null for NDEF_FORMATABLE until NfcUtil.parseMessage is refactored
        if (tag != null) {
          json.put("id", byteArrayToJSON(tag.getId()));
          json.put("techTypes", new JSONArray(Arrays.asList(tag.getTechList())));
        }

        json.put("type", translateType(ndef.getType()));
        json.put("maxSize", ndef.getMaxSize());
        json.put("isWritable", ndef.isWritable());
        json.put("ndefMessage", messageToJSON(ndef.getCachedNdefMessage()));
        // Workaround for bug in ICS (Android 4.0 and 4.0.1) where
        // mTag.getTagService(); of the Ndef object sometimes returns null
        // see http://issues.mroland.at/index.php?do=details&task_id=47
        try {
          json.put("canMakeReadOnly", ndef.canMakeReadOnly());
        } catch (NullPointerException e) {
          json.put("canMakeReadOnly", JSONObject.NULL);
        }
      } catch (JSONException e) {
        Log.e(TAG, "Failed to convert ndef into json: " + ndef.toString(), e);
      }
    }
    return json;
  }

  static JSONObject tagToJSON(Tag tag) {
    JSONObject json = new JSONObject();
    IsoDep tagStuff = IsoDep.get(tag);
    byte send_bytes[] = {0,0,0,0,0,0,10};
    byte response_bytes[] = null;
    byte id_send_bytes[] = { 0, 0, 0, 0, 0, 0,15, 0};
    byte id_response_bytes[] = null;
    String mynewid = "";

    if (tag != null) {
      try {
        json.put("id", byteArrayToJSON(tag.getId()));
        json.put("techTypes", new JSONArray(Arrays.asList(tag.getTechList())));

        tagStuff.connect();

        response_bytes = tagStuff.transceive(send_bytes);
        id_response_bytes = tagStuff.transceive(id_send_bytes);

        double tempC = myTable[(((((int)response_bytes[1]) & 0xFF) << 8) | (((int)response_bytes[2]) & 0xFF))];
        double tempF = Math.round(((tempC * 9 / 5.0) + 32) * 100) / 100;

        mynewid = new Long(((((long)id_response_bytes[1]) & 0xFF) << 40 |
        (((long)id_response_bytes[2]) & 0xFF) << 32 |
        (((long)id_response_bytes[3]) & 0xFF) << 24 |
        (((long)id_response_bytes[4]) & 0xFF) << 16 |
        (((long)id_response_bytes[5]) & 0xFF) << 8 |
        (((long)id_response_bytes[6]) & 0xFF))).toString();

        json.put("tempId", mynewid);
        json.put("celsius", tempC);
        json.put("fahrenheit", tempF);

        tagStuff.close();
      } catch (JSONException e) {
        Log.e(TAG, "Failed to convert tag into json: " + tag.toString(), e);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return json;
  }

  /* GLEASON from CCPi-Android START */
  public static double myTable[] = {
    0, 0, 0,
    0, 0, 0,
    0,              323.32, 309.69, 298.16, 288.23, 279.54,
    271.83, 264.92, 258.67, 252.99, 247.77, 242.97, 238.52,
    234.37, 230.50, 226.88, 223.46, 220.25, 217.20, 214.32,
    211.57, 208.96, 206.47, 204.10, 201.82, 199.64, 197.54,
    195.53, 193.59, 191.73, 189.92, 188.19, 186.51, 184.88,
    183.31, 181.78, 180.30, 178.87, 177.48, 176.12, 174.80,
    173.52, 172.27, 171.06, 169.87, 168.72, 167.59, 166.49,
    165.41, 164.36, 163.33, 162.32, 161.34, 160.38, 159.43,
    158.51, 157.61, 156.72, 155.85, 155.00, 154.16, 153.34,
    152.53, 151.74, 150.96, 150.20, 149.45, 148.71, 147.98,
    147.27, 146.56, 145.87, 145.19, 144.52, 143.86, 143.21,
    142.57, 141.94, 141.32, 140.71, 140.11, 139.51, 138.93,
    138.35, 137.78, 137.22, 136.66, 136.11, 135.57, 135.04,
    134.51, 133.99, 133.48, 132.97, 132.47, 131.98, 131.49,
    131.00, 130.53, 130.05, 129.59, 129.13, 128.67, 128.22,
    127.77, 127.33, 126.89, 126.46, 126.04, 125.61, 125.19,
    124.78, 124.37, 123.97, 123.56, 123.17, 122.77, 122.38,
    122.00, 121.62, 121.24, 120.86, 120.49, 120.12, 119.76,
    119.40, 119.04, 118.69, 118.34, 117.99, 117.64, 117.30,
    116.96, 116.63, 116.29, 115.96, 115.63, 115.31, 114.99,
    114.67, 114.35, 114.04, 113.73, 113.42, 113.11, 112.81,
    112.50, 112.20, 111.91, 111.61, 111.32, 111.03, 110.74,
    110.46, 110.17, 109.89, 109.61, 109.33, 109.06, 108.78,
    108.51, 108.24, 107.97, 107.71, 107.44, 107.18, 106.92,
    106.66, 106.41, 106.15, 105.90, 105.65, 105.40, 105.15,
    104.90, 104.66, 104.41, 104.17, 103.93, 103.69, 103.45,
    103.22, 102.98, 102.75, 102.52, 102.29, 102.06, 101.83,
    101.61, 101.38, 101.16, 100.94, 100.72, 100.50, 100.28,
    100.07, 99.85, 99.64, 99.42, 99.21, 99.00, 98.79, 98.59,
    98.38, 98.17, 97.97, 97.77, 97.56, 97.36, 97.16, 96.96,
    96.77, 96.57, 96.37, 96.18, 95.98, 95.79, 95.60, 95.41,
    95.22, 95.03, 94.84, 94.66, 94.47, 94.29, 94.10, 93.92,
    93.74, 93.56, 93.38, 93.20, 93.02, 92.84, 92.66, 92.49,
    92.31, 92.14, 91.97, 91.79, 91.62, 91.45, 91.28, 91.11,
    90.94, 90.78, 90.61, 90.44, 90.28, 90.11, 89.95, 89.79,
    89.62, 89.46, 89.30, 89.14, 88.98, 88.82, 88.66, 88.51,
    88.35, 88.19, 88.04, 87.88, 87.73, 87.58, 87.42, 87.27,
    87.12, 86.97, 86.82, 86.67, 86.52, 86.37, 86.23, 86.08,
    85.93, 85.79, 85.64, 85.50, 85.35, 85.21, 85.07, 84.92,
    84.78, 84.64, 84.50, 84.36, 84.22, 84.08, 83.94, 83.80,
    83.67, 83.53, 83.39, 83.26, 83.12, 82.99, 82.85, 82.72,
    82.59, 82.45, 82.32, 82.19, 82.06, 81.93, 81.80, 81.67,
    81.54, 81.41, 81.28, 81.15, 81.02, 80.90, 80.77, 80.65,
    80.52, 80.39, 80.27, 80.14, 80.02, 79.90, 79.77, 79.65,
    79.53, 79.41, 79.29, 79.17, 79.04, 78.92, 78.80, 78.69,
    78.57, 78.45, 78.33, 78.21, 78.09, 77.98, 77.86, 77.74,
    77.63, 77.51, 77.40, 77.28, 77.17, 77.06, 76.94, 76.83,
    76.72, 76.60, 76.49, 76.38, 76.27, 76.16, 76.05, 75.94,
    75.83, 75.72, 75.47, 75.39, 75.26, 75.14, 75.05, 74.93,
    74.82, 74.72, 74.62, 74.52, 74.42, 74.32, 74.20, 74.10,
    74.00, 73.89, 73.79, 73.70, 73.61, 73.50, 73.39, 73.29,
    73.19, 73.08, 72.99, 72.89, 72.79, 72.70, 72.60, 72.49,
    72.40, 72.31, 72.22, 72.11, 72.01, 71.91, 71.81, 71.72,
    71.61, 71.52, 71.44, 71.36, 71.23, 71.14, 71.05, 70.96,
    70.87, 70.78, 70.68, 70.58, 70.49, 70.40, 70.30, 70.21,
    70.11, 70.02, 69.93, 69.85, 69.76, 69.67, 69.58, 69.49,
    69.39, 69.31, 69.23, 69.13, 69.04, 68.97, 68.87, 68.78,
    68.69, 68.60, 68.50, 68.42, 68.33, 68.23, 68.16, 68.07,
    67.98, 67.89, 67.82, 67.72, 67.64, 67.56, 67.48, 67.39,
    67.31, 67.22, 67.13, 67.05, 66.96, 66.88, 66.79, 66.72,
    66.63, 66.55, 66.48, 66.40, 66.31, 66.23, 66.14, 66.06,
    65.99, 65.92, 65.84, 65.76, 65.69, 65.61, 65.55, 65.48,
    65.38, 65.28, 65.19, 65.12, 65.04, 64.96, 64.88, 64.80,
    64.72, 64.63, 64.54, 64.48, 64.40, 64.31, 64.23, 64.16,
    64.08, 63.99, 63.92, 63.84, 63.76, 63.69, 63.60, 63.53,
    63.46, 63.37, 63.30, 63.24, 63.16, 63.08, 63.00, 62.93,
    62.85, 62.77, 62.71, 62.63, 62.56, 62.49, 62.41, 62.34,
    62.27, 62.19, 62.11, 62.04, 61.97, 61.90, 61.83, 61.76,
    61.69, 61.61, 61.54, 61.46, 61.39, 61.32, 61.25, 61.18,
    61.11, 61.03, 60.96, 60.90, 60.82, 60.75, 60.68, 60.60,
    60.54, 60.48, 60.41, 60.33, 60.27, 60.19, 60.12, 60.07,
    60.00, 59.93, 59.86, 59.78, 59.71, 59.64, 59.58, 59.51,
    59.44, 59.38, 59.30, 59.24, 59.18, 59.11, 59.03, 58.97,
    58.90, 58.83, 58.77, 58.70, 58.63, 58.57, 58.50, 58.43,
    58.37, 58.30, 58.23, 58.18, 58.11, 58.03, 57.98, 57.92,
    57.84, 57.78, 57.72, 57.65, 57.60, 57.54, 57.47, 57.40,
    57.33, 57.26, 57.20, 57.15, 57.08, 57.02, 56.95, 56.89,
    56.83, 56.77, 56.70, 56.64, 56.58, 56.51, 56.45, 56.40,
    56.34, 56.28, 56.22, 56.15, 56.07, 56.02, 55.97, 55.91,
    55.85, 55.79, 55.72, 55.67, 55.62, 55.55, 55.49, 55.45,
    55.38, 55.30, 55.25, 55.19, 55.12, 55.07, 55.00, 54.92,
    54.88, 54.82, 54.75, 54.70, 54.63, 54.57, 54.52, 54.46,
    54.39, 54.33, 54.28, 54.21, 54.15, 54.09, 54.03, 53.98,
    53.93, 53.86, 53.80, 53.76, 53.68, 53.63, 53.58, 53.51,
    53.45, 53.41, 53.35, 53.28, 53.23, 53.17, 53.11, 53.06,
    53.00, 52.94, 52.89, 52.83, 52.77, 52.72, 52.67, 52.60,
    52.55, 52.50, 52.44, 52.38, 52.33, 52.27, 52.21, 52.16,
    52.11, 52.05, 52.00, 51.94, 51.88, 51.82, 51.78, 51.72,
    51.66, 51.61, 51.56, 51.50, 51.45, 51.40, 51.35, 51.29,
    51.24, 51.18, 51.13, 51.07, 51.02, 50.96, 50.91, 50.86,
    50.80, 50.75, 50.70, 50.65, 50.59, 50.55, 50.49, 50.43,
    50.39, 50.33, 50.27, 50.23, 50.17, 50.12, 50.06, 50.01,
    49.95, 49.91, 49.85, 49.80, 49.75, 49.70, 49.64, 49.60,
    49.54, 49.49, 49.44, 49.39, 49.34, 49.29, 49.24, 49.18,
    49.14, 49.08, 49.03, 48.98, 48.93, 48.88, 48.82, 48.78,
    48.73, 48.67, 48.63, 48.58, 48.52, 48.47, 48.43, 48.38,
    48.32, 48.27, 48.22, 48.17, 48.13, 48.07, 48.03, 47.99,
    47.94, 47.89, 47.84, 47.79, 47.74, 47.69, 47.65, 47.60,
    47.54, 47.50, 47.44, 47.39, 47.35, 47.30, 47.25, 47.20,
    47.14, 47.09, 47.05, 47.00, 46.95, 46.90, 46.85, 46.80,
    46.75, 46.71, 46.66, 46.61, 46.56, 46.52, 46.47, 46.42,
    46.38, 46.33, 46.28, 46.23, 46.18, 46.13, 46.09, 46.03,
    45.98, 45.94, 45.89, 45.84, 45.80, 45.75, 45.70, 45.67,
    45.61, 45.56, 45.52, 45.47, 45.42, 45.38, 45.33, 45.29,
    45.24, 45.20, 45.14, 45.10, 45.06, 45.01, 44.96, 44.92,
    44.88, 44.83, 44.78, 44.73, 44.69, 44.65, 44.60, 44.55,
    44.51, 44.46, 44.42, 44.38, 44.33, 44.28, 44.24, 44.19,
    44.14, 44.11, 44.06, 44.01, 43.96, 43.92, 43.87, 43.83,
    43.79, 43.74, 43.70, 43.66, 43.61, 43.56, 43.52, 43.48,
    43.43, 43.39, 43.34, 43.30, 43.26, 43.21, 43.17, 43.12,
    43.08, 43.04, 42.99, 42.94, 42.90, 42.86, 42.82, 42.78,
    42.73, 42.69, 42.65, 42.60, 42.56, 42.52, 42.47, 42.42,
    42.39, 42.35, 42.30, 42.26, 42.22, 42.17, 42.13, 42.10,
    42.05, 42.01, 41.96, 41.91, 41.87, 41.83, 41.78, 41.75,
    41.71, 41.66, 41.63, 41.60, 41.55, 41.49, 41.47, 41.42,
    41.38, 41.35, 41.30, 41.26, 41.22, 41.17, 41.12, 41.09,
    41.04, 40.99, 40.96, 40.92, 40.88, 40.84, 40.79, 40.75,
    40.71, 40.68, 40.63, 40.58, 40.54, 40.49, 40.46, 40.42,
    40.37, 40.33, 40.30, 40.25, 40.21, 40.17, 40.13, 40.09,
    40.05, 40.00, 39.96, 39.93, 39.88, 39.84, 39.81, 39.76,
    39.72, 39.68, 39.64, 39.59, 39.56, 39.52, 39.47, 39.44,
    39.40, 39.35, 39.32, 39.28, 39.24, 39.20, 39.16, 39.12,
    39.08, 39.04, 39.00, 38.96, 38.92, 38.88, 38.84, 38.81,
    38.76, 38.72, 38.68, 38.64, 38.60, 38.57, 38.53, 38.48,
    38.45, 38.41, 38.37, 38.33, 38.30, 38.25, 38.21, 38.17,
    38.14, 38.10, 38.06, 38.02, 37.98, 37.94, 37.90, 37.86,
    37.83, 37.79, 37.75, 37.71, 37.67, 37.63, 37.60, 37.56,
    37.52, 37.48, 37.44, 37.40, 37.37, 37.33, 37.29, 37.25,
    37.21, 37.17, 37.14, 37.10, 37.06, 37.03, 36.98, 36.95,
    36.91, 36.87, 36.84, 36.79, 36.76, 36.72, 36.68, 36.65,
    36.61, 36.57, 36.54, 36.50, 36.46, 36.43, 36.39, 36.35,
    36.31, 36.27, 36.23, 36.20, 36.16, 36.13, 36.09, 36.06,
    36.02, 35.98, 35.95, 35.91, 35.88, 35.84, 35.80, 35.76,
    35.73, 35.69, 35.65, 35.62, 35.58, 35.54, 35.50, 35.47,
    35.43, 35.39, 35.36, 35.32, 35.28, 35.26, 35.22, 35.17,
    35.14, 35.10, 35.06, 35.03, 34.99, 34.96, 34.92, 34.89,
    34.85, 34.81, 34.78, 34.74, 34.70, 34.67, 34.63, 34.60,
    34.56, 34.52, 34.49, 34.45, 34.41, 34.38, 34.35, 34.31,
    34.27, 34.24, 34.20, 34.16, 34.13, 34.10, 34.06, 34.02,
    33.99, 33.95, 33.92, 33.88, 33.85, 33.81, 33.78, 33.74,
    33.70, 33.67, 33.64, 33.60, 33.56, 33.53, 33.49, 33.46,
    33.42, 33.39, 33.35, 33.32, 33.29, 33.25, 33.22, 33.18,
    33.15, 33.11, 33.07, 33.04, 33.01, 32.97, 32.94, 32.91,
    32.87, 32.84, 32.80, 32.77, 32.73, 32.70, 32.67, 32.63,
    32.60, 32.56, 32.53, 32.49, 32.46, 32.43, 32.39, 32.35,
    32.32, 32.29, 32.25, 32.22, 32.18, 32.15, 32.12, 32.08,
    32.05, 32.02, 31.98, 31.95, 31.92, 31.89, 31.85, 31.81,
    31.78, 31.75, 31.71, 31.68, 31.64, 31.61, 31.58, 31.55,
    31.52, 31.49, 31.45, 31.41, 31.38, 31.35, 31.31, 31.28,
    31.25, 31.21, 31.18, 31.14, 31.11, 31.08, 31.05, 31.02,
    30.99, 30.95, 30.92, 30.88, 30.85, 30.82, 30.78, 30.75,
    30.73, 30.70, 30.66, 30.63, 30.59, 30.55, 30.52, 30.49,
    30.45, 30.43, 30.39, 30.36, 30.33, 30.30, 30.26, 30.23,
    30.20, 30.16, 30.13, 30.10, 30.07, 30.03, 30.00, 29.96,
    29.93, 29.90, 29.87, 29.84, 29.81, 29.77, 29.74, 29.71,
    29.67, 29.64, 29.61, 29.58, 29.54, 29.51, 29.48, 29.45,
    29.42, 29.38, 29.36, 29.32, 29.28, 29.26, 29.23, 29.19,
    29.16, 29.13, 29.10, 29.07, 29.04, 29.01, 28.97, 28.93,
    28.91, 28.88, 28.84, 28.81, 28.78, 28.75, 28.71, 28.68,
    28.65, 28.62, 28.59, 28.56, 28.53, 28.50, 28.46, 28.43,
    28.40, 28.37, 28.34, 28.31, 28.28, 28.24, 28.21, 28.18,
    28.15, 28.12, 28.09, 28.06, 28.03, 27.99, 27.96, 27.93,
    27.90, 27.87, 27.84, 27.80, 27.77, 27.74, 27.71, 27.68,
    27.65, 27.62, 27.59, 27.57, 27.53, 27.50, 27.47, 27.44,
    27.41, 27.38, 27.34, 27.31, 27.28, 27.25, 27.22, 27.19,
    27.16, 27.13, 27.10, 27.07, 27.03, 27.00, 26.97, 26.95,
    26.92, 26.89, 26.86, 26.83, 26.80, 26.76, 26.73, 26.70,
    26.67, 26.64, 26.61, 26.58, 26.55, 26.52, 26.49, 26.46,
    26.43, 26.40, 26.37, 26.34, 26.32, 26.29, 26.26, 26.22,
    26.19, 26.16, 26.13, 26.10, 26.08, 26.05, 26.02, 25.99,
    25.95, 25.92, 25.90, 25.86, 25.84, 25.80, 25.77, 25.74,
    25.71, 25.68, 25.65, 25.62, 25.60, 25.57, 25.53, 25.51,
    25.48, 25.45, 25.42, 25.39, 25.35, 25.32, 25.30, 25.27,
    25.24, 25.21, 25.18, 25.15, 25.12, 25.09, 25.06, 25.03,
    25.00, 24.97, 24.94, 24.90, 24.88, 24.85, 24.82, 24.79,
    24.77, 24.74, 24.70, 24.67, 24.64, 24.61, 24.59, 24.56,
    24.53, 24.49, 24.47, 24.44, 24.40, 24.38, 24.35, 24.32,
    24.29, 24.27, 24.24, 24.21, 24.18, 24.15, 24.12, 24.09,
    24.07, 24.03, 24.00, 23.97, 23.94, 23.91, 23.89, 23.86,
    23.82, 23.80, 23.77, 23.74, 23.71, 23.68, 23.65, 23.62,
    23.60, 23.57, 23.54, 23.52, 23.48, 23.45, 23.42, 23.40,
    23.37, 23.34, 23.32, 23.28, 23.26, 23.23, 23.19, 23.17,
    23.15, 23.12, 23.08, 23.05, 23.03, 23.00, 22.97, 22.95,
    22.92, 22.89, 22.86, 22.83, 22.80, 22.77, 22.75, 22.72,
    22.69, 22.66, 22.64, 22.61, 22.58, 22.55, 22.52, 22.49,
    22.46, 22.43, 22.41, 22.38, 22.35, 22.32, 22.29, 22.26,
    22.24, 22.21, 22.18, 22.16, 22.13, 22.10, 22.08, 22.05,
    22.02, 21.99, 21.96, 21.93, 21.91, 21.87, 21.85, 21.82,
    21.79, 21.77, 21.74, 21.71, 21.69, 21.66, 21.63, 21.61,
    21.58, 21.55, 21.52, 21.49, 21.46, 21.43, 21.40, 21.37,
    21.35, 21.32, 21.29, 21.27, 21.24, 21.21, 21.19, 21.16,
    21.13, 21.10, 21.08, 21.05, 21.02, 20.99, 20.97, 20.94,
    20.91, 20.88, 20.86, 20.83, 20.80, 20.78, 20.75, 20.72,
    20.69, 20.67, 20.64, 20.61, 20.58, 20.55, 20.52, 20.49,
    20.47, 20.45, 20.42, 20.39, 20.36, 20.33, 20.31, 20.28,
    20.26, 20.23, 20.20, 20.17, 20.15, 20.12, 20.09, 20.07,
    20.04, 20.01, 19.99, 19.96, 19.93, 19.90, 19.87, 19.84,
    19.82, 19.80, 19.77, 19.74, 19.71, 19.69, 19.66, 19.63,
    19.60, 19.58, 19.55, 19.52, 19.50, 19.47, 19.44, 19.42,
    19.39, 19.36, 19.34, 19.31, 19.29, 19.26, 19.23, 19.21,
    19.17, 19.15, 19.12, 19.09, 19.06, 19.04, 19.02, 18.99,
    18.96, 18.94, 18.91, 18.88, 18.86, 18.83, 18.80, 18.78,
    18.75, 18.72, 18.70, 18.67, 18.64, 18.61, 18.59, 18.56,
    18.54, 18.51, 18.49, 18.46, 18.44, 18.41, 18.38, 18.35,
    18.32, 18.29, 18.27, 18.25, 18.22, 18.19, 18.17, 18.14,
    18.11, 18.09, 18.06, 18.03, 18.01, 17.99, 17.96, 17.93,
    17.91, 17.88, 17.86, 17.83, 17.81, 17.78, 17.74, 17.72,
    17.70, 17.67, 17.64, 17.62, 17.59, 17.57, 17.54, 17.52,
    17.49, 17.47, 17.44, 17.41, 17.39, 17.36, 17.33, 17.31,
    17.28, 17.25, 17.22, 17.20, 17.17, 17.15, 17.12, 17.10,
    17.07, 17.05, 17.02, 16.99, 16.97, 16.94, 16.92, 16.89,
    16.86, 16.84, 16.81, 16.78, 16.76, 16.74, 16.71, 16.69,
    16.67, 16.63, 16.60, 16.58, 16.55, 16.52, 16.50, 16.48,
    16.45, 16.42, 16.40, 16.37, 16.34, 16.32, 16.29, 16.27,
    16.24, 16.22, 16.20, 16.17, 16.14, 16.12, 16.09, 16.07,
    16.04, 16.01, 15.99, 15.96, 15.94, 15.91, 15.88, 15.85,
    15.83, 15.81, 15.79, 15.76, 15.73, 15.70, 15.68, 15.66,
    15.64, 15.61, 15.58, 15.55, 15.52, 15.50, 15.48, 15.45,
    15.42, 15.40, 15.37, 15.35, 15.32, 15.30, 15.27, 15.25,
    15.22, 15.20, 15.17, 15.14, 15.12, 15.09, 15.07, 15.05,
    15.02, 14.99, 14.97, 14.94, 14.92, 14.89, 14.87, 14.84,
    14.82, 14.79, 14.77, 14.74, 14.72, 14.69, 14.67, 14.65,
    14.62, 14.60, 14.57, 14.54, 14.52, 14.49, 14.46, 14.44,
    14.42, 14.39, 14.37, 14.34, 14.32, 14.29, 14.27, 14.24,
    14.21, 14.19, 14.16, 14.14, 14.12, 14.09, 14.07, 14.04,
    14.02, 13.99, 13.97, 13.95, 13.92, 13.89, 13.87, 13.84,
    13.81, 13.79, 13.77, 13.74, 13.72, 13.69, 13.67, 13.64,
    13.62, 13.60, 13.58, 13.55, 13.52, 13.50, 13.48, 13.45,
    13.42, 13.40, 13.37, 13.35, 13.32, 13.29, 13.27, 13.25,
    13.22, 13.20, 13.17, 13.15, 13.12, 13.10, 13.08, 13.05,
    13.03, 13.00, 12.97, 12.95, 12.92, 12.90, 12.87, 12.85,
    12.82, 12.80, 12.78, 12.76, 12.73, 12.70, 12.68, 12.65,
    12.63, 12.61, 12.58, 12.55, 12.53, 12.51, 12.48, 12.46,
    12.44, 12.41, 12.39, 12.36, 12.34, 12.31, 12.29, 12.26,
    12.24, 12.22, 12.19, 12.17, 12.15, 12.12, 12.09, 12.07,
    12.04, 12.02, 12.00, 11.97, 11.95, 11.92, 11.89, 11.87,
    11.85, 11.82, 11.80, 11.78, 11.75, 11.73, 11.70, 11.68,
    11.65, 11.62, 11.60, 11.58, 11.56, 11.53, 11.50, 11.48,
    11.46, 11.44, 11.41, 11.38, 11.36, 11.33, 11.31, 11.29,
    11.26, 11.23, 11.21, 11.19, 11.16, 11.14, 11.12, 11.09,
    11.07, 11.04, 11.02, 10.99, 10.97, 10.95, 10.92, 10.90,
    10.87, 10.85, 10.83, 10.80, 10.78, 10.75, 10.73, 10.70,
    10.68, 10.66, 10.63, 10.61, 10.58, 10.56, 10.54, 10.51,
    10.49, 10.47, 10.44, 10.42, 10.39, 10.37, 10.34, 10.32,
    10.30, 10.27, 10.25, 10.22, 10.20, 10.18, 10.15, 10.13,
    10.11, 10.08, 10.06, 10.03, 10.01, 9.98, 9.96, 9.93, 9.91,
    9.88, 9.86, 9.83, 9.81, 9.79, 9.77, 9.75, 9.72, 9.70,
    9.68, 9.65, 9.62, 9.60, 9.58, 9.55, 9.53, 9.50, 9.48,
    9.45, 9.43, 9.41, 9.38, 9.36, 9.33, 9.31, 9.29, 9.27,
    9.24, 9.22, 9.19, 9.17, 9.15, 9.13, 9.10, 9.08, 9.05,
    9.02, 9.00, 8.98, 8.96, 8.93, 8.91, 8.88, 8.86, 8.84,
    8.82, 8.79, 8.77, 8.74, 8.71, 8.68, 8.66, 8.65, 8.63,
    8.60, 8.58, 8.55, 8.52, 8.50, 8.48, 8.45, 8.43, 8.40,
    8.38, 8.35, 8.33, 8.31, 8.29, 8.26, 8.24, 8.22, 8.19,
    8.16, 8.14, 8.12, 8.10, 8.07, 8.05, 8.03, 8.00, 7.98,
    7.95, 7.93, 7.91, 7.88, 7.86, 7.84, 7.81, 7.79, 7.77,
    7.74, 7.72, 7.70, 7.67, 7.65, 7.62, 7.60, 7.58, 7.56,
    7.53, 7.51, 7.48, 7.46, 7.43, 7.41, 7.39, 7.36, 7.34,
    7.32, 7.29, 7.27, 7.25, 7.23, 7.20, 7.18, 7.15, 7.13,
    7.11, 7.09, 7.06, 7.04, 7.01, 6.99, 6.97, 6.94, 6.92,
    6.90, 6.87, 6.85, 6.83, 6.80, 6.78, 6.76, 6.73, 6.71,
    6.69, 6.66, 6.64, 6.62, 6.59, 6.57, 6.55, 6.52, 6.50,
    6.48, 6.46, 6.43, 6.41, 6.38, 6.36, 6.33, 6.31, 6.29,
    6.26, 6.24, 6.22, 6.19, 6.17, 6.14, 6.13, 6.10, 6.08,
    6.06, 6.04, 6.01, 5.99, 5.97, 5.94, 5.92, 5.90, 5.88,
    5.85, 5.82, 5.80, 5.77, 5.75, 5.72, 5.69, 5.68, 5.66,
    5.64, 5.61, 5.59, 5.57, 5.54, 5.51, 5.49, 5.47, 5.45,
    5.43, 5.40, 5.38, 5.35, 5.33, 5.31, 5.28, 5.26, 5.23,
    5.21, 5.19, 5.17, 5.15, 5.12, 5.10, 5.08, 5.06, 5.03,
    5.00, 4.98, 4.96, 4.94, 4.91, 4.89, 4.87, 4.84, 4.82,
    4.80, 4.77, 4.76, 4.73, 4.70, 4.68, 4.65, 4.63, 4.61,
    4.58, 4.56, 4.54, 4.52, 4.50, 4.47, 4.45, 4.43, 4.40,
    4.38, 4.35, 4.33, 4.30, 4.28, 4.26, 4.24, 4.22, 4.19,
    4.16, 4.15, 4.12, 4.10, 4.07, 4.05, 4.03, 4.01, 3.99,
    3.96, 3.94, 3.91, 3.89, 3.87, 3.84, 3.82, 3.80, 3.77,
    3.75, 3.72, 3.70, 3.68, 3.66, 3.64, 3.61, 3.59, 3.57,
    3.54, 3.52, 3.50, 3.48, 3.45, 3.43, 3.41, 3.38, 3.36,
    3.34, 3.31, 3.29, 3.27, 3.25, 3.22, 3.19, 3.17, 3.15,
    3.13, 3.10, 3.08, 3.05, 3.03, 3.01, 2.99, 2.96, 2.94,
    2.92, 2.90, 2.87, 2.85, 2.83, 2.81, 2.78, 2.76, 2.74,
    2.71, 2.69, 2.67, 2.64, 2.62, 2.60, 2.58, 2.55, 2.53,
    2.51, 2.48, 2.46, 2.44, 2.42, 2.40, 2.37, 2.35, 2.32,
    2.29, 2.27, 2.25, 2.23, 2.21, 2.18, 2.16, 2.13, 2.11,
    2.09, 2.07, 2.05, 2.02, 2.00, 1.97, 1.95, 1.93, 1.90,
    1.88, 1.86, 1.84, 1.81, 1.78, 1.76, 1.74, 1.72, 1.70,
    1.68, 1.65, 1.63, 1.60, 1.58, 1.56, 1.53, 1.51, 1.49,
    1.47, 1.44, 1.42, 1.40, 1.37, 1.35, 1.33, 1.30, 1.28,
    1.26, 1.24, 1.22, 1.19, 1.17, 1.14, 1.12, 1.09, 1.07,
    1.05, 1.03, 1.00, 0.98, 0.96, 0.93, 0.91, 0.89, 0.86,
    0.84, 0.82, 0.79, 0.77, 0.75, 0.72, 0.70, 0.68, 0.66,
    0.63, 0.61, 0.59, 0.56, 0.54, 0.52, 0.49, 0.47, 0.45,
    0.43, 0.40, 0.38, 0.36, 0.33, 0.31, 0.29, 0.27, 0.24,
    0.22, 0.20, 0.17, 0.15, 0.13, 0.11, 0.09, 0.06, 0.04,
    0.01, -0.01, -0.04, -0.06, -0.08, -0.10, -0.13, -0.15,
    -0.17, -0.19, -0.22, -0.24, -0.27, -0.29, -0.31, -0.33,
    -0.35, -0.38, -0.40, -0.43, -0.44, -0.47, -0.49, -0.52,
    -0.54, -0.57, -0.59, -0.61, -0.64, -0.65, -0.67, -0.70,
    -0.72, -0.75, -0.77, -0.79, -0.82, -0.84, -0.86, -0.89,
    -0.91, -0.93, -0.95, -0.98, -1.00, -1.02, -1.05, -1.07,
    -1.09, -1.12, -1.14, -1.16, -1.18, -1.20, -1.22, -1.24,
    -1.27, -1.29, -1.31, -1.34, -1.36, -1.38, -1.41, -1.44,
    -1.46, -1.48, -1.50, -1.53, -1.55, -1.58, -1.60, -1.62,
    -1.64, -1.67, -1.69, -1.71, -1.73, -1.76, -1.79, -1.81,
    -1.83, -1.85, -1.88, -1.90, -1.93, -1.95, -1.97, -1.99,
    -2.01, -2.03, -2.06, -2.08, -2.11, -2.13, -2.16, -2.18,
    -2.20, -2.22, -2.24, -2.27, -2.29, -2.31, -2.34, -2.36,
    -2.38, -2.41, -2.43, -2.45, -2.48, -2.50, -2.52, -2.54,
    -2.56, -2.59, -2.62, -2.64, -2.66, -2.68, -2.70, -2.73,
    -2.75, -2.77, -2.80, -2.82, -2.84, -2.86, -2.89, -2.92,
    -2.94, -2.96, -2.98, -3.01, -3.03, -3.05, -3.07, -3.10,
    -3.12, -3.15, -3.17, -3.19, -3.21, -3.23, -3.25, -3.28,
    -3.30, -3.33, -3.35, -3.38, -3.39, -3.42, -3.44, -3.46,
    -3.49, -3.51, -3.54, -3.56, -3.58, -3.60, -3.63, -3.65,
    -3.68, -3.70, -3.72, -3.74, -3.76, -3.79, -3.81, -3.83,
    -3.85, -3.88, -3.90, -3.93, -3.95, -3.97, -3.99, -4.02,
    -4.04, -4.07, -4.09, -4.11, -4.14, -4.16, -4.18, -4.21,
    -4.23, -4.26, -4.28, -4.30, -4.32, -4.34, -4.37, -4.39,
    -4.42, -4.44, -4.46, -4.48, -4.50, -4.53, -4.55, -4.58,
    -4.60, -4.63, -4.65, -4.67, -4.69, -4.72, -4.74, -4.76,
    -4.79, -4.81, -4.84, -4.86, -4.88, -4.91, -4.93, -4.95,
    -4.97, -4.99, -5.01, -5.04, -5.06, -5.08, -5.11, -5.13,
    -5.16, -5.19, -5.21, -5.23, -5.25, -5.28, -5.30, -5.32,
    -5.35, -5.37, -5.40, -5.42, -5.44, -5.46, -5.49, -5.51,
    -5.53, -5.56, -5.58, -5.61, -5.63, -5.65, -5.67, -5.70,
    -5.72, -5.75, -5.76, -5.78, -5.81, -5.84, -5.86, -5.89,
    -5.91, -5.93, -5.95, -5.98, -6.00, -6.02, -6.05, -6.07,
    -6.09, -6.12, -6.14, -6.16, -6.19, -6.21, -6.23, -6.26,
    -6.28, -6.31, -6.33, -6.36, -6.38, -6.40, -6.42, -6.45,
    -6.47, -6.49, -6.52, -6.54, -6.57, -6.59, -6.61, -6.63,
    -6.66, -6.68, -6.71, -6.73, -6.75, -6.77, -6.80, -6.82,
    -6.85, -6.87, -6.90, -6.92, -6.94, -6.96, -6.99, -7.01,
    -7.03, -7.06, -7.08, -7.11, -7.13, -7.15, -7.18, -7.20,
    -7.23, -7.25, -7.28, -7.30, -7.32, -7.34, -7.37, -7.39,
    -7.42, -7.44, -7.46, -7.49, -7.51, -7.53, -7.55, -7.58,
    -7.60, -7.62, -7.65, -7.67, -7.70, -7.72, -7.75, -7.77,
    -7.79, -7.81, -7.84, -7.86, -7.88, -7.91, -7.93, -7.95,
    -7.98, -8.00, -8.03, -8.05, -8.08, -8.10, -8.12, -8.14,
    -8.17, -8.19, -8.21, -8.23, -8.26, -8.29, -8.31, -8.33,
    -8.36, -8.38, -8.40, -8.43, -8.45, -8.47, -8.50, -8.52,
    -8.54, -8.57, -8.60, -8.62, -8.64, -8.67, -8.69, -8.71,
    -8.73, -8.76, -8.78, -8.81, -8.83, -8.85, -8.88, -8.90,
    -8.92, -8.94, -8.97, -9.00, -9.02, -9.04, -9.07, -9.09,
    -9.12, -9.13, -9.16, -9.18, -9.21, -9.24, -9.27, -9.29,
    -9.31, -9.33, -9.35, -9.38, -9.40, -9.43, -9.46, -9.48,
    -9.50, -9.52, -9.54, -9.57, -9.59, -9.62, -9.65, -9.66,
    -9.69, -9.71, -9.74, -9.76, -9.78, -9.80, -9.83, -9.86,
    -9.89, -9.91, -9.93, -9.96, -9.98, -10.00, -10.03, -10.05,
    -10.07, -10.10, -10.13, -10.15, -10.17, -10.20, -10.22,
    -10.25, -10.27, -10.29, -10.32, -10.34, -10.36, -10.39,
    -10.42, -10.44, -10.46, -10.49, -10.51, -10.53, -10.56,
    -10.58, -10.60, -10.63, -10.65, -10.68, -10.70, -10.72,
    -10.75, -10.77, -10.80, -10.82, -10.84, -10.87, -10.90,
    -10.92, -10.94, -10.96, -10.99, -11.02, -11.04, -11.06,
    -11.09, -11.11, -11.14, -11.16, -11.19, -11.21, -11.24,
    -11.26, -11.28, -11.30, -11.33, -11.36, -11.38, -11.40,
    -11.43, -11.46, -11.48, -11.50, -11.52, -11.55, -11.58,
    -11.60, -11.63, -11.65, -11.68, -11.70, -11.72, -11.75,
    -11.77, -11.79, -11.82, -11.84, -11.87, -11.89, -11.92,
    -11.94, -11.97, -11.99, -12.01, -12.04, -12.06, -12.09,
    -12.11, -12.14, -12.16, -12.19, -12.21, -12.24, -12.26,
    -12.29, -12.31, -12.34, -12.36, -12.39, -12.41, -12.44,
    -12.46, -12.49, -12.51, -12.53, -12.55, -12.58, -12.60,
    -12.64, -12.66, -12.68, -12.71, -12.73, -12.75, -12.78,
    -12.81, -12.83, -12.85, -12.87, -12.90, -12.93, -12.96,
    -12.98, -13.00, -13.03, -13.06, -13.09, -13.11, -13.13,
    -13.16, -13.18, -13.20, -13.23, -13.26, -13.28, -13.30,
    -13.33, -13.35, -13.38, -13.40, -13.43, -13.45, -13.48,
    -13.50, -13.53, -13.56, -13.58, -13.60, -13.62, -13.65,
    -13.67, -13.70, -13.73, -13.75, -13.77, -13.80, -13.83,
    -13.86, -13.88, -13.90, -13.93, -13.95, -13.98, -14.00,
    -14.03, -14.05, -14.07, -14.10, -14.13, -14.16, -14.18,
    -14.20, -14.23, -14.25, -14.28, -14.31, -14.33, -14.35,
    -14.38, -14.40, -14.43, -14.46, -14.48, -14.51, -14.53,
    -14.56, -14.59, -14.61, -14.63, -14.66, -14.68, -14.71,
    -14.73, -14.76, -14.79, -14.81, -14.83, -14.86, -14.89,
    -14.91, -14.94, -14.96, -14.99, -15.02, -15.04, -15.06,
    -15.09, -15.12, -15.15, -15.17, -15.20, -15.23, -15.25,
    -15.28, -15.30, -15.33, -15.35, -15.37, -15.40, -15.42,
    -15.45, -15.47, -15.50, -15.52, -15.56, -15.59, -15.60,
    -15.63, -15.66, -15.69, -15.71, -15.74, -15.77, -15.79,
    -15.81, -15.84, -15.87, -15.89, -15.92, -15.95, -15.97,
    -16.00, -16.02, -16.04, -16.07, -16.10, -16.12, -16.15,
    -16.17, -16.20, -16.22, -16.25, -16.27, -16.30, -16.33,
    -16.35, -16.38, -16.41, -16.43, -16.46, -16.49, -16.51,
    -16.53, -16.56, -16.58, -16.61, -16.64, -16.67, -16.69,
    -16.72, -16.74, -16.77, -16.79, -16.82, -16.85, -16.87,
    -16.90, -16.92, -16.95, -16.97, -16.99, -17.03, -17.05,
    -17.08, -17.11, -17.14, -17.16, -17.18, -17.21, -17.24,
    -17.26, -17.29, -17.32, -17.34, -17.37, -17.39, -17.42,
    -17.45, -17.48, -17.50, -17.52, -17.55, -17.58, -17.61,
    -17.63, -17.66, -17.68, -17.71, -17.74, -17.77, -17.79,
    -17.82, -17.84, -17.87, -17.89, -17.91, -17.95, -17.98,
    -18.01, -18.03, -18.06, -18.09, -18.11, -18.15, -18.17,
    -18.20, -18.22, -18.25, -18.27, -18.29, -18.32, -18.35,
    -18.38, -18.40, -18.43, -18.45, -18.48, -18.51, -18.54,
    -18.57, -18.60, -18.62, -18.65, -18.68, -18.70, -18.73,
    -18.75, -18.78, -18.81, -18.84, -18.86, -18.89, -18.92,
    -18.94, -18.97, -19.00, -19.02, -19.06, -19.09, -19.11,
    -19.13, -19.16, -19.19, -19.22, -19.24, -19.27, -19.30,
    -19.33, -19.35, -19.38, -19.40, -19.43, -19.46, -19.48,
    -19.51, -19.54, -19.57, -19.60, -19.63, -19.65, -19.68,
    -19.71, -19.74, -19.76, -19.79, -19.82, -19.85, -19.87,
    -19.89, -19.92, -19.95, -19.98, -20.01, -20.04, -20.06,
    -20.09, -20.11, -20.15, -20.18, -20.20, -20.22, -20.25,
    -20.28, -20.31, -20.33, -20.36, -20.39, -20.42, -20.45,
    -20.47, -20.50, -20.53, -20.56, -20.59, -20.61, -20.64,
    -20.66, -20.69, -20.73, -20.76, -20.78, -20.80, -20.84,
    -20.87, -20.89, -20.92, -20.95, -20.98, -21.00, -21.03,
    -21.06, -21.09, -21.12, -21.15, -21.17, -21.20, -21.23,
    -21.26, -21.29, -21.32, -21.34, -21.37, -21.41, -21.44,
    -21.46, -21.48, -21.52, -21.54, -21.57, -21.59, -21.62,
    -21.65, -21.68, -21.71, -21.74, -21.77, -21.79, -21.82,
    -21.85, -21.88, -21.90, -21.94, -21.97, -22.00, -22.03,
    -22.06, -22.08, -22.12, -22.14, -22.17, -22.20, -22.24,
    -22.26, -22.29, -22.31, -22.35, -22.37, -22.39, -22.43,
    -22.46, -22.49, -22.52, -22.55, -22.58, -22.61, -22.63,
    -22.67, -22.70, -22.73, -22.75, -22.78, -22.81, -22.83,
    -22.86, -22.89, -22.92, -22.95, -22.98, -23.01, -23.04,
    -23.07, -23.10, -23.13, -23.16, -23.19, -23.22, -23.25,
    -23.28, -23.31, -23.33, -23.36, -23.39, -23.42, -23.45,
    -23.48, -23.51, -23.54, -23.57, -23.60, -23.63, -23.65,
    -23.68, -23.71, -23.75, -23.77, -23.80, -23.83, -23.86,
    -23.89, -23.92, -23.95, -23.99, -24.01, -24.04, -24.08,
    -24.11, -24.13, -24.16, -24.19, -24.22, -24.25, -24.28,
    -24.32, -24.34, -24.37, -24.40, -24.43, -24.46, -24.49,
    -24.53, -24.54, -24.58, -24.62, -24.64, -24.67, -24.71,
    -24.74, -24.76, -24.80, -24.83, -24.86, -24.89, -24.92,
    -24.95, -24.98, -25.00, -25.03, -25.07, -25.10, -25.14,
    -25.17, -25.19, -25.23, -25.26, -25.29, -25.32, -25.35,
    -25.38, -25.41, -25.44, -25.47, -25.51, -25.54, -25.56,
    -25.59, -25.62, -25.65, -25.69, -25.72, -25.75, -25.78,
    -25.81, -25.85, -25.88, -25.91, -25.94, -25.97, -26.00,
    -26.04, -26.07, -26.09, -26.12, -26.15, -26.19, -26.22,
    -26.24, -26.28, -26.32, -26.35, -26.38, -26.41, -26.44,
    -26.47, -26.50, -26.53, -26.57, -26.60, -26.63, -26.67,
    -26.69, -26.72, -26.75, -26.78, -26.82, -26.85, -26.88,
    -26.91, -26.94, -26.97, -27.00, -27.03, -27.06, -27.09,
    -27.13, -27.17, -27.20, -27.24, -27.26, -27.30, -27.33,
    -27.36, -27.39, -27.42, -27.46, -27.49, -27.53, -27.56,
    -27.58, -27.62, -27.65, -27.69, -27.72, -27.75, -27.79,
    -27.81, -27.85, -27.89, -27.92, -27.95, -27.97, -28.01,
    -28.04, -28.07, -28.11, -28.14, -28.17, -28.20, -28.24,
    -28.28, -28.31, -28.35, -28.37, -28.40, -28.44, -28.47,
    -28.50, -28.54, -28.57, -28.60, -28.64, -28.67, -28.71,
    -28.74, -28.77, -28.81, -28.85, -28.87, -28.90, -28.93,
    -28.97, -29.01, -29.04, -29.07, -29.11, -29.14, -29.17,
    -29.21, -29.24, -29.28, -29.31, -29.34, -29.38, -29.42,
    -29.45, -29.49, -29.52, -29.55, -29.58, -29.62, -29.66,
    -29.68, -29.72, -29.76, -29.79, -29.83, -29.86, -29.89,
    -29.93, -29.96, -30.00, -30.03, -30.06, -30.10, -30.14,
    -30.17, -30.21, -30.24, -30.26, -30.30, -30.34, -30.37,
    -30.42, -30.45, -30.48, -30.52, -30.56, -30.59, -30.63,
    -30.66, -30.69, -30.73, -30.76, -30.79, -30.83, -30.86,
    -30.89, -30.93, -30.96, -31.00, -31.04, -31.07, -31.10,
    -31.15, -31.18, -31.21, -31.25, -31.30, -31.33, -31.36,
    -31.40, -31.44, -31.47, -31.51, -31.54, -31.57, -31.61,
    -31.65, -31.69, -31.73, -31.76, -31.80, -31.82, -31.87,
    -31.91, -31.94, -31.98, -32.01, -32.05, -32.09, -32.13,
    -32.16, -32.20, -32.23, -32.26, -32.30, -32.35, -32.39,
    -32.42, -32.46, -32.49, -32.53, -32.57, -32.60, -32.63,
    -32.66, -32.71, -32.75, -32.78, -32.81, -32.85, -32.89,
    -32.92, -32.96, -33.00, -33.04, -33.08, -33.11, -33.15,
    -33.19, -33.22, -33.26, -33.30, -33.35, -33.37, -33.41,
    -33.46, -33.49, -33.53, -33.57, -33.60, -33.64, -33.68,
    -33.72, -33.77, -33.80, -33.83, -33.87, -33.90, -33.95,
    -33.98, -34.01, -34.05, -34.10, -34.14, -34.19, -34.22,
    -34.25, -34.29, -34.34, -34.38, -34.41, -34.45, -34.50,
    -34.54, -34.57, -34.62, -34.66, -34.70, -34.73, -34.76,
    -34.81, -34.85, -34.89, -34.93, -34.97, -35.02, -35.06,
    -35.10, -35.13, -35.17, -35.21, -35.24, -35.28, -35.33,
    -35.37, -35.40, -35.44, -35.48, -35.52, -35.57, -35.61,
    -35.65, -35.68, -35.73, -35.77, -35.81, -35.85, -35.89,
    -35.93, -35.98, -36.02, -36.04, -36.09, -36.14, -36.18,
    -36.22, -36.26, -36.31, -36.34, -36.38, -36.42, -36.46,
    -36.50, -36.54, -36.58, -36.63, -36.67, -36.70, -36.75,
    -36.80, -36.84, -36.88, -36.93, -36.97, -36.99, -37.45,
    -37.50, -37.55, -37.59, -37.64, -37.69, -37.74, -37.79,
    -37.83, -37.88, -37.93, -37.98, -38.03, -38.08, -38.13,
    -38.18, -38.22, -38.27, -38.32, -38.37, -38.42, -38.47,
    -38.52, -38.57, -38.62, -38.67, -38.72, -38.77, -38.82,
    -38.87, -38.92, -38.97, -39.02, -39.08, -39.13, -39.18,
    -39.23, -39.28, -39.33, -39.38, -39.43, -39.49, -39.54,
    -39.59, -39.64, -39.69, -39.75, -39.80, -39.85, -39.90,
    -39.96, -40.01, -40.06, -40.12, -40.17, -40.22, -40.28,
    -40.33, -40.38, -40.44, -40.49, -40.55, -40.60, -40.65,
    -40.71, -40.76, -40.82, -40.87, -40.93, -40.98, -41.04,
    -41.09, -41.15, -41.20, -41.26, -41.32, -41.37, -41.43,
    -41.48, -41.54, -41.60, -41.65, -41.71, -41.77, -41.83,
    -41.88, -41.94, -42.00, -42.06, -42.11, -42.17, -42.23,
    -42.29, -42.35, -42.41, -42.46, -42.52, -42.58, -42.64,
    -42.70, -42.76, -42.82, -42.88, -42.94, -43.00, -43.06,
    -43.12, -43.18, -43.24, -43.30, -43.37, -43.43, -43.49,
    -43.55, -43.61, -43.67, -43.74, -43.80, -43.86, -43.92,
    -43.99, -44.05, -44.11, -44.18, -44.24, -44.30, -44.37,
    -44.43, -44.50, -44.56, -44.63, -44.69, -44.76, -44.82,
    -44.89, -44.95, -45.02, -45.09, -45.15, -45.22, -45.29,
    -45.35, -45.42, -45.49, -45.56, -45.62, -45.69, -45.76,
    -45.83, -45.90, -45.97, -46.04, -46.10, -46.17, -46.24,
    -46.31, -46.39, -46.46, -46.53, -46.60, -46.67, -46.74,
    -46.81, -46.88, -46.96, -47.03, -47.10, -47.18, -47.25,
    -47.32, -47.40, -47.47, -47.55, -47.62, -47.69, -47.77,
    -47.85, -47.92, -48.00, -48.07, -48.15, -48.23, -48.31,
    -48.38, -48.46, -48.54, -48.62, -48.70, -48.78, -48.85,
    -48.93, -49.01, -49.09, -49.18, -49.26, -49.34, -49.42,
    -49.50, -49.58, -49.67, -49.75, -49.83, -49.92, -50.00,
    -50.09, -50.17, -50.26, -50.34, -50.43, -50.51, -50.60,
    -50.69, -50.77, -50.86, -50.95, -51.04, -51.13, -51.22,
    -51.31, -51.40, -51.49, -51.58, -51.67, -51.76, -51.86,
    -51.95, -52.04, -52.14, -52.23, -52.33, -52.42, -52.52,
    -52.61, -52.71, -52.81, -52.91, -53.00, -53.10, -53.20,
    -53.30, -53.40, -53.50, -53.61, -53.71, -53.81, -53.91,
    -54.02, -54.12, -54.23, -54.33, -54.44, -54.55, -54.65,
    -54.76, -54.87, -54.98, -55.09, -55.20, -55.31, -55.42,
    -55.54, -55.65, -55.77, -55.88, -56.00, -56.11, -56.23,
    -56.35, -56.47, -56.59, -56.71, -56.83, -56.95, -57.07,
    -57.20, -57.32, -57.45, -57.57, -57.70, -57.83, -57.96,
    -58.09, -58.22, -58.35, -58.49, -58.62, -58.76, -58.89,
    -59.03, -59.17, -59.31, -59.45, -59.59, -59.74, -59.88,
    -60.03, -60.17, -60.32, -60.47, -60.62, -60.77, -60.93,
    -61.08, -61.24, -61.40, -61.56, -61.72, -61.88, -62.05,
    -62.21, -62.38, -62.55, -62.72, -62.89, -63.07, -63.25,
    -63.43, -63.61, -63.79, -63.97, -64.16, -64.35, -64.54,
    -64.74, -64.93, -65.13, -65.33, -65.54, -65.74, -65.95,
    -66.17, -66.38, -66.60, -66.82, -67.04, -67.27, -67.50,
    -67.74, -67.98, -68.22, -68.47, -68.72, -68.97, -69.23,
    -69.49, -69.76, -70.03, -70.31, -70.60, -70.88, -71.18,
    -71.48, -71.79, -72.10, -72.42, -72.75, -73.08, -73.43,
    -73.78, -74.14, -74.51, -74.89, -75.28, -75.68, -76.10,
    -76.52, -76.96, -77.42, -77.89, -78.38, -78.88, -79.41,
    -79.95, -80.52, -81.12, -81.74, -82.40, -83.09, -83.82,
    -84.59, -85.42, -86.30, -87.26, -88.29, -89.42, -90.66,
    -92.06, -93.64, -95.48, -97.69, -100.46, -104.23, -110.34
  };
  /* GLEASON from CCPi-Android END */

  static String translateType(String type) {
    String translation;
    if (type.equals(Ndef.NFC_FORUM_TYPE_1)) {
      translation = "NFC Forum Type 1";
    } else if (type.equals(Ndef.NFC_FORUM_TYPE_2)) {
      translation = "NFC Forum Type 2";
    } else if (type.equals(Ndef.NFC_FORUM_TYPE_3)) {
      translation = "NFC Forum Type 3";
    } else if (type.equals(Ndef.NFC_FORUM_TYPE_4)) {
      translation = "NFC Forum Type 4";
    } else {
      translation = type;
    }
    return translation;
  }

  static NdefRecord[] jsonToNdefRecords(String ndefMessageAsJSON) throws JSONException {
    JSONArray jsonRecords = new JSONArray(ndefMessageAsJSON);
    NdefRecord[] records = new NdefRecord[jsonRecords.length()];
    for (int i = 0; i < jsonRecords.length(); i++) {
      JSONObject record = jsonRecords.getJSONObject(i);
      byte tnf = (byte) record.getInt("tnf");
      byte[] type = jsonToByteArray(record.getJSONArray("type"));
      byte[] id = jsonToByteArray(record.getJSONArray("id"));
      byte[] payload = jsonToByteArray(record.getJSONArray("payload"));
      records[i] = new NdefRecord(tnf, type, id, payload);
    }
    return records;
  }

  static JSONArray byteArrayToJSON(byte[] bytes) {
    JSONArray json = new JSONArray();
    for (byte aByte : bytes) {
      json.put(aByte);
    }
    return json;
  }

  static byte[] jsonToByteArray(JSONArray json) throws JSONException {
    byte[] b = new byte[json.length()];
    for (int i = 0; i < json.length(); i++) {
      b[i] = (byte) json.getInt(i);
    }
    return b;
  }

  static JSONArray messageToJSON(NdefMessage message) {
    if (message == null) {
      return null;
    }

    List<JSONObject> list = new ArrayList<JSONObject>();

    for (NdefRecord ndefRecord : message.getRecords()) {
      list.add(recordToJSON(ndefRecord));
    }

    return new JSONArray(list);
  }

  static JSONObject recordToJSON(NdefRecord record) {
    JSONObject json = new JSONObject();
    try {
      json.put("tnf", record.getTnf());
      json.put("type", byteArrayToJSON(record.getType()));
      json.put("id", byteArrayToJSON(record.getId()));
      json.put("payload", byteArrayToJSON(record.getPayload()));
    } catch (JSONException e) {
      //Not sure why this would happen, documentation is unclear.
      Log.e(TAG, "Failed to convert ndef record into json: " + record.toString(), e);
    }
    return json;
  }

}
