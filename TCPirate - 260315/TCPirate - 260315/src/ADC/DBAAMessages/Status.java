package ADC.DBAAMessages;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: tomg
 * Date: 09/09/2012
 * Time: 18:20:55
 * To change this template use File | Settings | File Templates.
 */
public class Status extends Message{

    public Status()
    {
        try{
        statusCode = StatusCodes.AnswerReceived;
        requestID = "";
        }
        catch (Exception e) { System.out.println(e.toString());}
    }

    public Status(StatusCodes code, String id)
    {
        statusCode = code;
        requestID = id;
    }

    public String requestID;

    public StatusCodes statusCode;

    public enum StatusCodes {
        PassedChallenge,
        FailedChallenge,
        ChallengeSent,
        AnswerReceived,
        VerifyingAnswer,
        UserUnavailable
    }

    //public static HashMap<StatusCodes, String> StatusMessages;
    //static {
//        StatusMessages.put(StatusCodes.PassedChallenge , "Passed challenge");
//        StatusMessages.put(StatusCodes.FailedChallenge, "Failed challenge");
//        StatusMessages.put(StatusCodes.ChallengeSent, "Challenge sent to client");
//        StatusMessages.put(StatusCodes.AnswerReceived, "Client returned an answer");
//        StatusMessages.put(StatusCodes.VerifyingAnswer, "Verifying challenge");
//        StatusMessages.put(StatusCodes.UserUnavailable, "Cannot contact client on side channel");
//    }

    public void Execute()
    {
        return;
    }
}
