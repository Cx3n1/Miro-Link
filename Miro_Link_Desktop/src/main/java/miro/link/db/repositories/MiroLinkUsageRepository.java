package miro.link.db.repositories;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miro.link.db.DatabaseManager;
import miro.link.db.model.MiroLinkUsage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static miro.link.db.DatabaseManager.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MiroLinkUsageRepository {
    private static MiroLinkUsage CURRENT_USER_DATA;

    //**Initialisation**\\
    static{
        createTableIfItsNotInDatabase();
    }

    public static void initialise(){
        CURRENT_USER_DATA = fetchUserDataCreateIfNone();
    }

    private static boolean createTableIfItsNotInDatabase() {
        if(!isDatabaseInitialised()) return false;

        try(Statement statement = getConnection().createStatement()){
            return statement.execute(DatabaseManager.getQueryCreateMiroLinkUsageTable());
        } catch (Exception e) {
            log.warn("Couldn't create miro_link_usage table", e);
        }

        return false;
    }

    private static MiroLinkUsage fetchUserDataCreateIfNone() {
        if(!isDatabaseInitialised()) return null;

        if(userIsInDatabase()){
            return getUserUsage();
        } else{
            return createNewUserInDatabaseAndReturnIt();
        }
    }

    private static MiroLinkUsage createNewUserInDatabaseAndReturnIt() {
        try(PreparedStatement preparedStatement = getConnection()
                .prepareStatement(DatabaseManager.getQueryInsertIntoMluTable())){
            int userID = getUserId();
            int usages = 0;
            long duration = 0;

            preparedStatement.setInt(1,userID);
            preparedStatement.setInt(2,usages);
            preparedStatement.setLong(3,duration);

            preparedStatement.execute();

            return new MiroLinkUsage(userID, usages, duration);
        } catch (Exception e) {
            log.warn("Couldn't add new user", e);
        }
        return null;
    }

    //**Queries**\\
    public static List<MiroLinkUsage> getAll(){
        if(!isDatabaseInitialised()) return null;

        List<MiroLinkUsage> result = null;

        try(Statement statement = getConnection().createStatement()){
            ResultSet resultSet = statement.executeQuery(DatabaseManager.getQuerySelectAll());

            result = new ArrayList<>();

            int userID;
            int usages;
            long totalUsageTime;

            while (resultSet.next()){
                userID = resultSet.getInt("USER_ID");
                usages = resultSet.getInt("MIRO_LINK_USAGES");
                totalUsageTime = resultSet.getLong("TOTAL_MIRRORING_DURATION");

                result.add(new MiroLinkUsage(userID, usages, totalUsageTime));
            }

        } catch (Exception e) {
            log.warn("Couldn't fetch data from database", e);
        }

        return result;
    }

    public static MiroLinkUsage getUserUsage(){
        return getUserUsage(getUserId());
    }
    public static MiroLinkUsage getUserUsage(int ID){
        if(!isDatabaseInitialised()) return null;

        try(PreparedStatement preparedStatement = getConnection()
                .prepareStatement(DatabaseManager.getQueryUserUsage())){
            //user will always be registered in database (initialisation makes sure of that)

            preparedStatement.setInt(1, ID);

            ResultSet resultSet = preparedStatement.executeQuery();

            int userID = resultSet.getInt("USER_ID");
            int usages = resultSet.getInt("MIRO_LINK_USAGES");
            long totalUsageTime = resultSet.getLong("TOTAL_MIRRORING_DURATION");

            return new MiroLinkUsage(userID, usages, totalUsageTime);

        } catch (Exception e) {
            log.warn("Couldn't fetch data from database", e);
        }
        return null;
    }

    public static boolean incrementUserUsagesBy(int amount){
        return updateUserUsages(CURRENT_USER_DATA.getUsages() + amount);
    }
    public static boolean updateUserUsages(int amount){
        return updateUserUsages(getUserId(), amount);
    }
    public static boolean updateUserUsages(int ID, int amount){
        if(!isDatabaseInitialised()) return false;
        try(PreparedStatement preparedStatement = getConnection()
                .prepareStatement(DatabaseManager.getQueryUpdateMluById())){
            preparedStatement.setString(1, "MIRO_LINK_USAGES");
            preparedStatement.setInt(2, amount);
            preparedStatement.setInt(3, ID);

            return preparedStatement.execute();
        } catch (Exception e) {
            log.warn("Couldn't update user usages", e);
        }
        return false;
    }

    public static boolean incrementUserTotalUsageTimeBy(long time){
        return updateUserTotalUsageTime(CURRENT_USER_DATA.getTotalUsageTime() + time);
    }
    public static boolean updateUserTotalUsageTime(long time){
        return updateUserTotalUsageTime(getUserId(), time);
    }
    public static boolean updateUserTotalUsageTime(int ID, long time){
        if(!isDatabaseInitialised()) return false;

        try(PreparedStatement preparedStatement = getConnection()
                .prepareStatement(DatabaseManager.getQueryUpdateMluById())){
            preparedStatement.setString(1, "TOTAL_MIRRORING_DURATION");
            preparedStatement.setLong(2, time);
            preparedStatement.setInt(3, ID);

            return preparedStatement.execute();
        } catch (Exception e) {
            log.warn("Couldn't update user usage time", e);
        }
        return false;
    }

    //**Utility**\\
    private static boolean userIsInDatabase() {
        return userIsInDatabase(getUserId());
    }
    private static boolean userIsInDatabase(int userID) {
        try(PreparedStatement preparedStatement = getConnection()
                .prepareStatement(DatabaseManager.getQueryUserUsage())){

            preparedStatement.setInt(1, userID);

            return preparedStatement.execute();
        } catch (Exception e) {
            log.warn("Couldn't fetch data from database", e);
        }
        return false;
    }

}
