package ADC.DBAAMessages;

/**
 * Created by IntelliJ IDEA.
 * User: tomg
 * Date: 09/09/2012
 * Time: 13:40:47
 * To change this template use File | Settings | File Templates.
 */
public class ReCAPTCHARequest extends ChallengeRequest {

    public ReCAPTCHARequest(SideChannelType sideChannelType, String sideChannelID) {
            super(sideChannelType,sideChannelID);
    }

    public void Execute()
    {
        return;
    }
}
