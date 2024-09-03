package com.voting;

import com.voting.Vote;

import java.io.*;
import java.util.*;

public class VoteSystem {
    private static final List<Vote> votes = new ArrayList<>();
    private static final String DATA_FILE = "votes.dat";
    private static final String USERS_FILE = "users.dat";
    private static final Set<String> USERS = new HashSet<>();
    private static Map<String, List<Vote>> votingRounds = new HashMap<>();

    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "password";
    private static final Map<String, Set<String>> userVotes = new HashMap<>();

    static {
        loadUsers();
        loadVotes(); // Load previous voting rounds if available
    }

    private static void saveVotes() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(votingRounds);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadVotes() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object obj = ois.readObject();
                if (obj instanceof Map) {
                    votingRounds = (Map<String, List<Vote>>) obj;
                } else {
                    votingRounds = new HashMap<>();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(USERS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadUsers() {
        File file = new File(USERS_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object obj = ois.readObject();
                if (obj instanceof Set) {
                    USERS.addAll((Set<String>) obj);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Add default user if not already present
        if (!USERS.contains(DEFAULT_USERNAME + ":" + DEFAULT_PASSWORD)) {
            USERS.add(DEFAULT_USERNAME + ":" + DEFAULT_PASSWORD);
            saveUsers();
        }
    }

    private static boolean authenticateUser(String username, String password) {
        return USERS.contains(username.trim() + ":" + password.trim());
    }

    private static void addUser(String username, String password) {
        USERS.add(username.trim() + ":" + password.trim());
        saveUsers();
    }

    private static void deleteUser(String username) {
        USERS.removeIf(user -> user.split(":")[0].equals(username.trim()));
        saveUsers();
    }

    private static void deleteVotingRound(String roundName) {
        votingRounds.remove(roundName);
        saveVotes();
    }

    private static void viewUsers() {
        System.out.println("List of Users:");
        USERS.forEach(user -> System.out.println(user.split(":")[0]));
        System.out.println("-----------------------------");
    }

    private static void viewTopics() {
        System.out.println("Available Voting Rounds:");
        List<String> roundNames = new ArrayList<>(votingRounds.keySet());
        for (int i = 0; i < roundNames.size(); i++) {
            System.out.println((i + 1) + ": " + roundNames.get(i));
        }
        System.out.println("-----------------------------");
    }

    private static void deleteTopic(Scanner scanner) {
        viewTopics(); // View topics before deleting
        System.out.println("Enter the number of the topic you want to delete:");
        int topicIndex;
        try {
            topicIndex = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, try again.");
            return;
        }

        List<String> roundNames = new ArrayList<>(votingRounds.keySet());
        if (topicIndex >= 0 && topicIndex < roundNames.size()) {
            String roundToDelete = roundNames.get(topicIndex);
            deleteVotingRound(roundToDelete);
            System.out.println("Topic deleted successfully.");
        } else {
            System.out.println("Invalid topic number.");
        }
    }

    private static void voteOnTopics(String username, Scanner scanner) {
        System.out.println("Available Voting Rounds:");
        List<String> roundNames = new ArrayList<>(votingRounds.keySet());
        for (int i = 0; i < roundNames.size(); i++) {
            System.out.println((i + 1) + ": " + roundNames.get(i));
        }

        System.out.println("Enter the number of the round you want to vote on:");
        int selectedRoundIndex;
        try {
            selectedRoundIndex = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, try again.");
            return;
        }

        if (selectedRoundIndex < 0 || selectedRoundIndex >= roundNames.size()) {
            System.out.println("Invalid round number.");
            return;
        }

        String selectedRound = roundNames.get(selectedRoundIndex);
        List<Vote> roundVotes = votingRounds.get(selectedRound);

        if (userVotes.containsKey(username) && userVotes.get(username).contains(selectedRound)) {
            System.out.println("You have already voted in this round.");
            return;
        }

        System.out.println("Choose an option to vote (1 to " + roundVotes.size() + "):");
        for (int i = 0; i < roundVotes.size(); i++) {
            System.out.println((i + 1) + ": " + roundVotes.get(i).getOption());
        }

        String input = scanner.nextLine();

        try {
            int optionIndex = Integer.parseInt(input) - 1;
            if (optionIndex >= 0 && optionIndex < roundVotes.size()) {
                roundVotes.get(optionIndex).increment();
                System.out.println(
                        "Vote recorded for " + roundVotes.get(optionIndex).getOption() + " in " + selectedRound);
                userVotes.computeIfAbsent(username, k -> new HashSet<>()).add(selectedRound);
                saveVotes(); // Save votes after each voting round
            } else {
                System.out.println("Invalid option number.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, try again.");
        }
    }

    private static void viewResults() {
        for (Map.Entry<String, List<Vote>> entry : votingRounds.entrySet()) {
            System.out.println("Results for " + entry.getKey() + ":");
            for (Vote vote : entry.getValue()) {
                System.out.println(vote.getOption() + ": " + vote.getCount() + " votes");
            }
        }
        System.out.println("-----------------------------");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Welcome to the Voting System");
            System.out.println("Do you want to enter the Login Terminal? (Y/N):");
            String inp = scanner.nextLine().trim();
            if (inp.equalsIgnoreCase("N") || inp.equalsIgnoreCase("NO")) {
                System.out.println("Exiting...");
                System.exit(0);
            }

            System.out.println("Login as:");
            System.out.println("1. Admin");
            System.out.println("2. User");
            String loginChoice = scanner.nextLine().trim();

            System.out.println("Enter username:");
            String username = scanner.nextLine();
            System.out.println("Enter password:");
            String password = scanner.nextLine();

            if (loginChoice.equals("1")) {
                if (!username.equals(DEFAULT_USERNAME) || !authenticateUser(username, password)) {
                    System.out.println("Authentication failed. Exiting.");
                    return;
                }
            } else if (loginChoice.equals("2")) {
                if (!authenticateUser(username, password)) {
                    System.out.println("Authentication failed. Exiting.");
                    return;
                }
            } else {
                System.out.println("Invalid login choice. Exiting.");
                return;
            }

            System.out.println("-----------------------------");

            boolean isAdmin = username.equals(DEFAULT_USERNAME);

            while (true) {
                if (isAdmin) {
                    System.out.println("\n");
                    System.out.println("Admin Menu:");
                    System.out.println("1. Start a New Voting Round");
                    System.out.println("2. View Voting Topics");
                    System.out.println("3. Delete a Topic");
                    System.out.println("4. View Results of Voting");
                    System.out.println("5. View Users");
                    System.out.println("6. Add a New User");
                    System.out.println("7. Delete a User");
                    System.out.println("8. Logout");
                    System.out.println("\n");
                } else {
                    System.out.println("\n");
                    System.out.println("User Menu:");
                    System.out.println("1. Vote on Available Topics");
                    System.out.println("2. View Results of Voting");
                    System.out.println("3. Logout");
                    System.out.println("\n");
                }

                String choice = scanner.nextLine();
                System.out.println("-----------------------------");
                switch (choice) {
                    case "1":
                        if (isAdmin) {
                            System.out.println("Enter round name:");
                            String roundName = scanner.nextLine();
                            List<Vote> roundVotes = new ArrayList<>();
                            System.out.println("Enter the number of options for this round:");
                            int numOptions = scanner.nextInt();
                            scanner.nextLine(); // Consume newline

                            for (int i = 1; i <= numOptions; i++) {
                                System.out.println("Enter option " + i + ":");
                                String option = scanner.nextLine();
                                roundVotes.add(new Vote(option));
                            }

                            votingRounds.put(roundName, roundVotes);
                            System.out.println("Voting Round Started for " + roundName + "!");

                            saveVotes(); // Save current voting rounds
                        } else {
                            voteOnTopics(username, scanner);
                        }
                        break;

                    case "2":
                        if (isAdmin) {
                            viewTopics();
                        } else {
                            voteOnTopics(username, scanner);
                        }
                        break;

                    case "3":
                        if (isAdmin) {
                            deleteTopic(scanner); // Call deleteTopic with scanner
                        } else {
                            System.out.println("Logging out...");
                            break;
                        }
                        break;

                    case "4":
                        viewResults();
                        break;

                    case "5":
                        if (isAdmin) {
                            viewUsers();
                        } else {
                            System.out.println("Logging out...");
                            break;
                        }
                        break;

                    case "6":
                        if (isAdmin) {
                            System.out.println("Enter new username:");
                            String newUser = scanner.nextLine();
                            System.out.println("Enter new password:");
                            String newPassword = scanner.nextLine();
                            addUser(newUser, newPassword);
                            System.out.println("User added successfully.");
                        } else {
                            System.out.println("Logging out...");
                            break;
                        }
                        break;

                    case "7":
                        if (isAdmin) {
                            System.out.println("Enter username to delete:");
                            String userToDelete = scanner.nextLine();
                            deleteUser(userToDelete);
                            System.out.println("User deleted successfully.");
                        } else {
                            System.out.println("Invalid choice, try again.");
                        }
                        break;

                    case "8":
                        if (isAdmin) {
                            System.out.println("Logging out...");
                            break;
                        }
                        System.out.println("Invalid choice, try again.");
                        break;

                    default:
                        System.out.println("Invalid choice, try again.");
                        break;
                }

                if ((isAdmin && choice.equals("8")) || (!isAdmin && choice.equals("3"))) {
                    break; // Break the inner loop to go back to login
                }
            }
        }
    }
}
