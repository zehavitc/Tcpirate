package ADC.DBAAMessages;

import java.util.Date;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: tomg
 * Date: 09/09/2012
 * Time: 13:41:00
 * To change this template use File | Settings | File Templates.
 */
public abstract class ChallengeRequest extends Message {

    public String get_requestID() {
        return requestID;
    }

    public void set_requestID(String _requestID) {
        this.requestID = _requestID;
    }

    private String requestID;

    public enum SideChannelType { SMS, IM, Email}

    public String sideChannelID;
    public SideChannelType sideChannelType;

    public ChallengeRequest(SideChannelType type, String id)
    {
        Random random = new Random();
        String requestID = GenerateID(Integer.toString(random.nextInt()));
        this.set_requestID(requestID);
        sideChannelType = type;
        sideChannelID = id;
    }

    public String GenerateID(String randomNumber)
    {
        String strNow = new Date().toString();
        String strHash = strNow + randomNumber;
        String token = Hash.GetHash(strHash, Hash.HashType.SHA256);
        return token;
    }
}
