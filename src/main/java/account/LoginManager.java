package account;

import database.StorageHashTable;

/**
 *
 * @author Jiyansh
 */
public class LoginManager {
    final private StorageHashTable accountsTable;
    
    public LoginManager(String fileName, int maxNumAccounts) {
        accountsTable = new StorageHashTable(System.getProperty("user.dir"), fileName, maxNumAccounts, 100, 2, new int[]{100, 91});
    }
    
    /**
     * Adds account to database
     * @param name the name of person.
     * @param username their username. Duplicate usernames not allowed.
     * @param password their password as a char array for security. Note: This will be overwritten.
     * @return true if successfully saved.
     */
    public boolean addAccount(String name, String username, char[] password) {        
        if(accountsTable.containsKey(username)) {
            return false;
        }
        
        String hashedPassword = PasswordHash.hashPasswordWithSalt(password);        //Will overwrite password for security.
        String[] loginInfo = new String[] {name, hashedPassword};
        
        return accountsTable.addEntry(username, loginInfo) != -1;
    } 
    
    /**
     * Removes account from database.
     * @param username Username of person to remove.
     * @return True if successfully removed.
     */
    public boolean removeAccount(String username) {
        return accountsTable.deleteEntry(username) != null;
    }
    
    /**
     * Changes account name.
     * @param username The username of account to change.
     * @param newName The new name.
     * @return true if name changed.
     */
    public boolean changeName(String username, String newName) {
        String[] data = accountsTable.readEntry(username);
        data[0] = newName;
        
        return accountsTable.changeRecords(username, data) != null;
    }
    
    /**
     * Changes stored password. Requires old password to change.
     * @param username The username of account to change.
     * @param oldPassword The old password
     * @param newPassword The new password
     * @return true if password changed.
     */
    public boolean changePassword(String username, char[] oldPassword, char[] newPassword) {
        if (!authenticate(username, oldPassword)) {
            wipePassword(newPassword);
            return false;
        }
                
        String[] data = accountsTable.readEntry(username);
        data[1] = PasswordHash.hashPasswordWithSalt(newPassword);
        
        return accountsTable.changeRecords(username, data) != null;
    }
    
    /**
     * Checks if the given username and password match the stored values.
     * @param username the username to check
     * @param password the password to check. This will be overwritten.
     * @return true if they match the stored values.
     */
    public boolean authenticate(String username, char[] password) {
        if (!accountsTable.containsKey(username)) {
            return false;
        }
        return PasswordHash.matches(password, accountsTable.readEntry(username)[1]);
    }
    
    /**
     * @return true if any accounts exist in the database.
     */
    public boolean isEmpty() {
        return accountsTable.isEmpty();
    }
    
    /**
     * @param username the username to check.
     * @return true if username is already in the system.
     */
    public boolean usernameAlreadyExists(String username) {
        return accountsTable.containsKey(username);
    }   
    
    /**
     * Improves security by overwriting a password array with zeros.
     * @param password the password to wipe.
     */
    public static void wipePassword(char[] password) {
        PasswordHash.wipePassword(password);
    }
}
