public class MutualExclusionService {

    public static void csEnter(){
        // The first function call cs-enter() allows an application to request 
        // permission to start executing its critical section. The function call 
        // is blocking and returns only when the invoking application can execute its critical section.

    }

    public static void csLeave(){
        // The second function call cs-leave() allows an application to inform 
        // the service that it has finished executing its critical section.
    }
    
}
