package org.example;

import org.example.thread.Globals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

public class InputListener extends Thread {
    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(System.in));
            String input;
            do {
                try {
                    // wait until we have data to complete a readLine()
                    while (!br.ready()  /*  ADD SHUTDOWN CHECK HERE */) {
                        Thread.sleep(0);
                    }
                    input = br.readLine();

                    if (input.equalsIgnoreCase("exit")) {
                        System.out.println("Closing program...");
                        Globals.CLOSE_PROGRAM = true;
                        return;
                    } else {
                        System.out.println("You typed: " + input);
                    }
                } catch (InterruptedException e) {
                    //System.out.println("ConsoleInputReadTask() cancelled");
                    return;
                }
            } while (!Globals.CLOSE_PROGRAM);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
