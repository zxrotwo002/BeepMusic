import java.io.*;
import java.util.*;

public class Beep {

    //Change these
    private static final double multiplier = 1.5;
    private static final int partNumber = 1;
    private static double length = 0;


    public static void main(String[] args) throws IOException {
        Map<Integer, Integer> map = new HashMap<>();
        InputStream inputStream = Objects.requireNonNull(Beep.class.getResourceAsStream("/music.txt"));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        InputStream inputStream2 = Objects.requireNonNull(Beep.class.getResourceAsStream("/map.txt"));
        BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(inputStream2));
        ArrayList<String> strings1 = new ArrayList<>();
        ArrayList<String> strings2;
        ArrayList<String> strings3;
        int counter = 0;


        for (String line = bufferedReader2.readLine(); line != null; line = bufferedReader2.readLine()) {
            String[] values2 = line.split(";");
            map.put(Integer.parseInt(values2[0]),Integer.parseInt(values2[1]));
        }

        for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
            String[] values = line.split(", ");
            if (values.length == 6) {
                if ((values[2].equalsIgnoreCase("Note_on_c") || values[2].equalsIgnoreCase("Note_off_c"))&& values[0].equalsIgnoreCase(String.valueOf(partNumber))) {
                    int newTime = Integer.parseInt(values[1]);
                    int tone = Integer.parseInt(values[4]);
                    int velocity = Integer.parseInt(values[5]);
                    if (velocity > 10) {
                        strings1.add(counter, map.get(tone) + ";" + (newTime) + ";" + 1);
                    }
                    else {
                        strings1.add(counter, map.get(tone) + ";" + (newTime) + ";" + 0);
                    }
                    counter ++;
                }
            }
        }
        strings3 = sortOut(strings1);
        strings2 = convertTimes(strings3);

        writeBeepFile(strings2);

        bufferedReader.close();
    }

    private static ArrayList<String> sortOut(ArrayList<String> strings1) {
        ArrayList<String> strings = new ArrayList<>();
        List<Integer> frequencies = new ArrayList<>();
        List<Integer> times = new ArrayList<>();

        for (String s : strings1) {
            String[] values = s.split(";");
            int freq = Integer.parseInt(values[0]);
            int time = Integer.parseInt(values[1]);
            boolean state = values[2].equalsIgnoreCase("1");
            if (state) {
                if (!times.isEmpty()) {
                    if (times.getLast() < time) {
                        strings.add(frequencies.getLast() + ";" + time + ";" + 0);
                        frequencies.clear();
                        times.clear();
                        frequencies.add(freq);
                        times.add(time);
                        strings.add(freq + ";" + time + ";" + 1);
                    }
                    if (times.getLast() == time) {
                        strings.add(frequencies.getLast() + ";" + time + ";" + 0);
                        frequencies.clear();
                        times.clear();
                        frequencies.add(freq);
                        times.add(time);
                        strings.add(freq + ";" + time + ";" + 1);
                    }
                }
                else {
                    frequencies.add(freq);
                    times.add(time);
                    strings.add(freq + ";" + time + ";" + 1);
                }
            } else {
                if (frequencies.contains(freq) && times.contains(time)) {
                    frequencies.clear();
                    times.clear();
                    strings.add(freq + ";" + time + ";" + 0);
                }
            }

        }

        return strings;
    }

    private static ArrayList<String> convertTimes(ArrayList<String> strings1) {
        ArrayList<String> strings = new ArrayList<>();
        ArrayList<String> strings2 = new ArrayList<>();
        Map<Integer,Tone> toneMap = new HashMap<>();
        int maxTime = Integer.parseInt(strings1.getLast().split(";")[1]);
        for (int i = strings1.size(); i != 0 ; i--) {
            String[] values = strings1.get(i-1).split(";");
            if (values[2].equalsIgnoreCase("0")) {
                toneMap.put(Integer.parseInt(values[0]),new Tone(Integer.parseInt(values[0]), Integer.parseInt(values[1]),false));
            }
            else {
                int time;
                if (toneMap.get(Integer.parseInt(values[0])) != null) {
                    time = toneMap.get(Integer.parseInt(values[0])).time() - Integer.parseInt(values[1]);
                    toneMap.put(Integer.parseInt(values[0]),new Tone(Integer.parseInt(values[0]), Integer.parseInt(values[1]),true));
                }
                else {
                    toneMap.put(Integer.parseInt(values[0]),new Tone(Integer.parseInt(values[0]), Integer.parseInt(values[1]),true));
                    time = maxTime - Integer.parseInt(values[1]);
                }
                double finalTime = multiplier *  time;
                if (time > 0) {
                    if (finalTime > 20000.0) {
                        finalTime = 20000.0;
                    }
                    strings.add(String.valueOf(finalTime));
                    length += finalTime;
                    strings.add(values[0]);
                }
            }
        }
        for (int i = strings.size(); i != 0 ; i--) {
            strings2.add(strings.get(i-1));
        }
        return strings2;
    }

    public static void writeBeepFile(ArrayList<String> strings) throws IOException {
        File BeepFile = new File("beep.sh");

        BufferedWriter writer = new BufferedWriter(new FileWriter(BeepFile));
        writer.write("#!/bin/bash");
        writer.write("\n\n");
        writer.write("#Length: ");
        writer.write(String.valueOf(Math.round((length / 10)/100.0)));
        writer.write("s | ");
        writer.write(String.valueOf(Math.round(length / 1000)/60));
        writer.write(":");
        writer.write(String.valueOf(Math.round(((Math.round(((length / 1000)/60)*100)/100.0) - Math.floor((length / 1000)/60))*60)));
        writer.write("min");
        writer.write("\n");
        writer.write("#Part-Nr: ");
        writer.write(String.valueOf(partNumber));
        writer.write("\n");
        writer.write("#Speed-Multiplier: ");
        writer.write(String.valueOf(multiplier));
        writer.write("\n\n");
        writer.write("beep    -f ");
        double time = 0;
        String timeString;
        if (strings.size() > 1) {
            for (int i = 0; i < strings.size() - 2; i += 2) {
                writer.write(strings.get(i));
                for (int j = 0; j < 6 - strings.get(i).length(); j++) {
                    writer.write(" ");
                }
                writer.write("-l ");
                writer.write(String.valueOf(Math.round(Double.parseDouble(strings.get(i + 1))*10.0)/10.0));
                timeString = String.valueOf(Math.round((time / 10) / 100.0));
                time += Double.parseDouble(strings.get(i +1));
                for (int j = 0; j < 10 - (String.valueOf(Math.round(Double.parseDouble(strings.get(i + 1))*10.0)/10.0).length()); j++) {
                    writer.write(" ");
                }
                writer.write("`#Timestamp: ");
                writer.write(timeString);
                writer.write("s `");
                for (int k = 0; k < 10 - timeString.length(); k++) {
                    writer.write(" ");
                }
                writer.write("\\\n");
                writer.write("     -n -f ");
            }
            writer.write(strings.get(strings.size() - 2));
            for (int j = 0; j < 6 - strings.get(strings.size() - 2).length(); j++) {
                writer.write(" ");
            }
            writer.write("-l ");
            writer.write(String.valueOf(Math.round(Double.parseDouble(strings.getLast())*10.0)/10.0));
            timeString = String.valueOf(Math.round((time / 10) / 100.0));

            for (int j = 0; j < 10 - (String.valueOf(Math.round(Double.parseDouble(strings.getLast())*10.0)/10.0).length()); j++) {
                writer.write(" ");
            }
            writer.write("`#Timestamp: ");
            writer.write(timeString);
            writer.write("s `");
            for (int k = 0; k < 10 - timeString.length(); k++) {
                writer.write(" ");
            }
        }
        writer.write("\\\n");

        writer.close();
    }

    private record Tone(int frequency, int time, boolean state) {
    }
}
