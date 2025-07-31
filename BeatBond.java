import java.util.*;

// ================= User Class =================
class User {
    private String username;
    private String password;
    private Set<String> playlist;
    private LinkedList<String> recentSongs;
    private Set<User> friends;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.playlist = new HashSet<>();
        this.recentSongs = new LinkedList<>();
        this.friends = new HashSet<>();
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String pass) {
        return password.equals(pass);
    }

    public Set<User> getFriends() {
        return friends;
    }

    public Set<String> getPlaylist() {
        return playlist;
    }

    public LinkedList<String> getRecentSongs() {
        return recentSongs;
    }

    public void addFriend(User user) {
        if (!user.equals(this)) {
            friends.add(user);
        }
    }

    public void addSong(String song) {
        playlist.add(song);
        recentSongs.addFirst(song);
        if (recentSongs.size() > 5) {
            recentSongs.removeLast();
        }
    }
}

// ================= Song Manager =================
class SongManager {
    private static Map<String, Integer> songCount = new HashMap<>();

    public static void addSong(String song) {
        songCount.put(song, songCount.getOrDefault(song, 0) + 1);
    }

    public static List<String> getTrendingSongs() {
        List<String> sortedSongs = new ArrayList<>(songCount.keySet());
        sortedSongs.sort((a, b) -> songCount.get(b) - songCount.get(a));
        return sortedSongs;
    }

    public static int getSongCount(String song) {
        return songCount.getOrDefault(song, 0);
    }
}

// ================= Core System =================
class ChatterTunesSystem {
    private Map<String, User> users = new HashMap<>();
    private User currentUser = null;

    // Register user
    public String registerUser(String username, String password) {
        if (username == null || username.isBlank()) return "Invalid username!";
        if (password == null || password.isBlank()) return "Invalid password!";
        if (users.containsKey(username)) return "Username already exists!";
        users.put(username, new User(username, password));
        return "User registered successfully!";
    }

    // Login user
    public String login(String username, String password) {
        if (!users.containsKey(username)) return "User not found!";
        User user = users.get(username);
        if (!user.checkPassword(password)) return "Incorrect password!";
        currentUser = user;
        return "Logged in as " + username;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Add friend
    public String addFriend(String friendName) {
        if (!isLoggedIn()) return "Login first!";
        if (friendName.equals(currentUser.getUsername())) return "Cannot add yourself!";
        User friend = users.get(friendName);
        if (friend == null) return "User not found!";
        currentUser.addFriend(friend);
        friend.addFriend(currentUser);
        return "Friend added!";
    }

    // Add song
    public String addSong(String song) {
        if (!isLoggedIn()) return "Login first!";
        if (song == null || song.isBlank()) return "Invalid song name!";
        currentUser.addSong(song);
        SongManager.addSong(song);
        return "Song added to playlist!";
    }

    // View playlist
    public String viewPlaylist() {
        if (!isLoggedIn()) return "Login first!";
        return "Playlist: " + currentUser.getPlaylist() +
               "\nRecently Played: " + currentUser.getRecentSongs();
    }

    // Recommendations
    public String recommendSongs() {
        if (!isLoggedIn()) return "Login first!";
        Set<String> recommended = new HashSet<>();
        for (User friend : currentUser.getFriends()) {
            for (String song : friend.getPlaylist()) {
                if (!currentUser.getPlaylist().contains(song)) recommended.add(song);
            }
        }
        return recommended.isEmpty() ? "No recommendations right now."
                                     : "Recommended songs: " + recommended;
    }

    // Trending songs
    public String showTrendingSongs() {
        List<String> trending = SongManager.getTrendingSongs();
        if (trending.isEmpty()) return "No songs added yet!";
        StringBuilder sb = new StringBuilder("Trending Songs:\n");
        for (String song : trending) {
            sb.append(song).append(" (").append(SongManager.getSongCount(song)).append(" plays)\n");
        }
        return sb.toString();
    }

    // Music twin
    public String findMusicTwin() {
        if (!isLoggedIn()) return "Login first!";
        User twin = null;
        int maxCommon = -1;
        for (User friend : currentUser.getFriends()) {
            int common = 0;
            for (String song : currentUser.getPlaylist()) {
                if (friend.getPlaylist().contains(song)) common++;
            }
            if (common > maxCommon) {
                maxCommon = common;
                twin = friend;
            }
        }
        return (twin == null) ? "No music twin found yet!"
                              : "Your music twin is: " + twin.getUsername() +
                                " (" + maxCommon + " songs in common)";
    }
}

// ================= Main App =================
public class ChatterTunesApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ChatterTunesSystem system = new ChatterTunesSystem();

        while (true) {
            // Show main login/register menu
            System.out.println("\n=== Welcome to ChatterTunes ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choice: ");
            String input = sc.nextLine();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice!");
                continue;
            }

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter username: ");
                    String username = sc.nextLine();
                    System.out.print("Enter password: ");
                    String password = sc.nextLine();
                    System.out.println(system.registerUser(username, password));
                }
                case 2 -> {
                    System.out.print("Enter username: ");
                    String username = sc.nextLine();
                    System.out.print("Enter password: ");
                    String password = sc.nextLine();
                    String result = system.login(username, password);
                    System.out.println(result);
                    if (result.startsWith("Logged in")) {
                        loggedInMenu(system, sc);  // go to user dashboard
                    }
                }
                case 3 -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private static void loggedInMenu(ChatterTunesSystem system, Scanner sc) {
        while (true) {
            System.out.println("\n--- User Dashboard ---");
            System.out.println("1. Add Friend");
            System.out.println("2. Add Song to Playlist");
            System.out.println("3. View My Playlist");
            System.out.println("4. Recommend Songs");
            System.out.println("5. Show Trending Songs");
            System.out.println("6. Find My Music Twin");
            System.out.println("7. Logout");
            System.out.print("Choice: ");
            String input = sc.nextLine();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice!");
                continue;
            }

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter friend's username: ");
                    System.out.println(system.addFriend(sc.nextLine()));
                }
                case 2 -> {
                    System.out.print("Enter song name: ");
                    System.out.println(system.addSong(sc.nextLine()));
                }
                case 3 -> System.out.println(system.viewPlaylist());
                case 4 -> System.out.println(system.recommendSongs());
                case 5 -> System.out.println(system.showTrendingSongs());
                case 6 -> System.out.println(system.findMusicTwin());
                case 7 -> {
                    System.out.println("Logged out successfully.");
                    return; // back to login menu
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }
}
