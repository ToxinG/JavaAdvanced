package javax.management.remote.rmi;

public class RMIServerImplImpl extends RMIServerImpl {

	public RMIServerImplImpl (java.util.Map arg0)  {		super (arg0);
	}

	
	protected java.lang.String getProtocol ()  {
		return null;
	}
	
	protected void closeClient (javax.management.remote.rmi.RMIConnection arg0) throws java.io.IOException {
		return;
	}
	
	protected void closeServer () throws java.io.IOException {
		return;
	}
	
	protected void export () throws java.io.IOException {
		return;
	}
	
	protected javax.management.remote.rmi.RMIConnection makeClient (java.lang.String arg0, javax.security.auth.Subject arg1) throws java.io.IOException {
		return null;
	}
	
	public java.rmi.Remote toStub () throws java.io.IOException {
		return null;
	}
}
