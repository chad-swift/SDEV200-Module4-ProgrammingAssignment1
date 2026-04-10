package org.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

// Grouping Symbol Checker
// This checks whether a Java file source code has a complete set of matching each type of brace
// Chad Swift
// SDEV200-50P
// Module 4 Assignment 1

public class Main {

    public static void main(String[] args) throws Exception {
        // Since the program should be passed the file path as a command-line argument, we should check if it's there or not
        if (args.length == 0) {
            throw new Exception("There should be one command line argument, and it should be a filepath");
        }
        // Should only have one single arg
        if (args.length > 1) {
            System.out.println("More than one file path was provided to the program to check");
        }

        // Make a new checker, pass in the file from the args
        var checker = new GroupingSymbolsFileChecker(new File(args[0]));

        // print the source code to screen from file
        checker.printSourceCodeToScreen();
        // collect all the groupings
        checker.collectGroupingsFromFile();
        // run validation
        String validationString = checker.validateGroupingSymbols()
                ? "Correct Grouping Pairs"
                : "Incorrect Grouping Pairs";

        // print to the screen a readable version of the validation
        System.out.println(validationString);
    }

}

class GroupingSymbolsFileChecker {

    // store the filename
    File fileName;
    // static ArrayLists that hold the possible groupings for open and closed
    static final ArrayList<Character> possibleOpenGroupings = new ArrayList<>();
    static final ArrayList<Character> possibleClosedGroupings = new ArrayList<>();
    // collection for grouping symbols found in the file
    ArrayList<Character> groupingSymbols = new ArrayList<>();

    // This constructor was mainly for my own testing,
    // but of course having a no-arg constructor is always good anyway
    GroupingSymbolsFileChecker() {
        this.fileName = new File("/Users/chadswift/IdeaProjects/SDEV200-Module1-ProgrammingAssignment1/src/main/java/org/example/Main.java");
        possibleOpenGroupings.add('{');
        possibleOpenGroupings.add('(');
        possibleOpenGroupings.add('[');
        possibleClosedGroupings.add('}');
        possibleClosedGroupings.add(')');
        possibleClosedGroupings.add(']');

    }

    // constructor that builds from file
    GroupingSymbolsFileChecker(File file) {
        this.fileName = file;
        possibleOpenGroupings.add('{');
        possibleOpenGroupings.add('(');
        possibleOpenGroupings.add('[');
        possibleClosedGroupings.add('}');
        possibleClosedGroupings.add(')');
        possibleClosedGroupings.add(']');

    }

    /** Method that displays the groupings to make sure they have been collected
     * @return string formatted array of all collected grouping symbols
     */
    public String getGroupingList() {
        return groupingSymbols.toString();
    }

    /** This prints the source code being tested to screen
     * @throws Exception if file is not valid when used in scanner
     */
    public void printSourceCodeToScreen() throws Exception {
        // initialize a scanner
        Scanner input = new Scanner(fileName);

        System.out.println("Source Code being Tested: ");

        // if the file exists, go through each of the lines while it has a next line and print the next line to the screen
        if (fileName.exists()) {
            while (input.hasNextLine()) {
                System.out.println(input.nextLine());
            }
        } else {
            // if it doesn't exist, tell hte user
            System.out.println("File " + fileName + " does not exist");
        }
    }

    /** Method that collects the groupings from the file being tested
     * @throws Exception if file does not exist when used with scanner
     */
    public void collectGroupingsFromFile() throws Exception {
        // Define new scanner
        Scanner input = new Scanner(fileName);

        // Rules for grouping:
        // you can keep opening grouping symbols infinitely within each other
        // example, you can open a (, then open a {, then open a [. But the next symbol that MUST resolve
        // is the last one open, so in the last example we have ({[, meaning the next symbol MUST be ] to close the last opened [
        // result ({[]})
        // a grouping can be freestanding in quotes, so we should skip if ever there's a quote
        // a grouping can be freestanding in comment, so we should skip if there's a line that starts with a comment

        // this flag signals if we are in a quote or not
        boolean isInQuote = false;

        // looping on line instead of word so that we can skip the line if it starts with a comment
        // bit overhead-y, but if you were to test this code on this exact file with the rules above, it would fail otherwise
        while (input.hasNextLine()) {
            String line = input.nextLine();
            // continue if the line starts with a comment
            if (line.startsWith("/") || line.startsWith("//") || line.startsWith("*")) {
                continue;
            }
            // take the line, split it into words
            for (String word : line.split("/s")) {
                // take the word, split it into chars
                for (char token : word.toCharArray()) {
                    // if the token is a quote, we want to flip the boolean until we hit the next quote
                    if (token == '"') {
                        isInQuote = !isInQuote;
                    }

                    // if the token is an open or closed grouping, store it in the collection
                    // UNLESS we're currently in a quote, in which we will continue
                    if (possibleOpenGroupings.contains(token) || possibleClosedGroupings.contains(token)) {
                        if (isInQuote) {
                            continue;
                        }
                        groupingSymbols.add(token);
                    }
                }
            }
        }
    }


    /** Private testing logic to ensure an even number of opening and closing brackets
     * @return true if the amount of closing grouping symbols and the amount of opening grouping symbols are equal
     */
    private boolean closingAndOpeningPairsMatch() {
        var openGroupingSymbols = new ArrayList<Character>();
        var closedGroupingSymbols = new ArrayList<Character>();

        for (var token : groupingSymbols) {
            if (possibleOpenGroupings.contains(token)) {
                openGroupingSymbols.add(token);
            }
            if (possibleClosedGroupings.contains(token)) {
                closedGroupingSymbols.add(token);
            }
        }

        return openGroupingSymbols.size() == closedGroupingSymbols.size();
    }

    /**
     * @return True if the rules for validation all pass, groups are even and groups don't overlap
     */
    public boolean validateGroupingSymbols() {
        // if there aren't an equal amount of closing and opening groupings, we should just return false
        // no need to check any further
        if (!closingAndOpeningPairsMatch()) {
            return false;
        }

        // create a new stack
        var groupStack = new Stack<Character>();

        // iterate over the groupingSymbols collection
        for (char token : groupingSymbols) {
            // if the token is an open symbol
            if (possibleOpenGroupings.contains(token)) {
                // add the matching closing symbol to the stack. The indices of openings and closings
                // are the same symbol
                groupStack.add(possibleClosedGroupings.get(possibleOpenGroupings.indexOf(token)));
            }
            // if the token is a closing symbol
            if (possibleClosedGroupings.contains(token)) {
                // we want to pop the top off the stack
                // if it is not equal to the current token,
                // we can just return false early
                if (token != groupStack.pop()) {
                    return false;
                }
            }
        }
        // without any issues of the open grouping symbol not matching its closing mate, the stack will be
        // added to and popped repeatedly until the end of the file is reached. If this happens, this
        // method will return true. Because we already checked if there is an equal number of openings to closings,
        // there is no fear of having a stack that has symbols left in it, nor an error trying to perform pop on a stack
        // when it's empty. Freestanding grouping symbols in comments and quotes were never collected.
        return true;
    }
}