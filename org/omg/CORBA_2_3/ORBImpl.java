package org.omg.CORBA_2_3;

public class ORBImpl extends ORB {

	public ORBImpl ()  {		super ();
	}

	
	public boolean poll_next_response ()  {
		return false;
	}
	
	public org.omg.CORBA.Object resolve_initial_references (java.lang.String arg0) throws org.omg.CORBA.ORBPackage.InvalidName {
		return null;
	}
	
	public void send_multiple_requests_deferred (org.omg.CORBA.Request[] arg0)  {
		return;
	}
	
	public void send_multiple_requests_oneway (org.omg.CORBA.Request[] arg0)  {
		return;
	}
	
	protected void set_parameters (java.applet.Applet arg0, java.util.Properties arg1)  {
		return;
	}
	
	protected void set_parameters (java.lang.String[] arg0, java.util.Properties arg1)  {
		return;
	}
	
	public org.omg.CORBA.Object string_to_object (java.lang.String arg0)  {
		return null;
	}
	
	public org.omg.CORBA.TypeCode create_alias_tc (java.lang.String arg0, java.lang.String arg1, org.omg.CORBA.TypeCode arg2)  {
		return null;
	}
	
	public org.omg.CORBA.Any create_any ()  {
		return null;
	}
	
	public org.omg.CORBA.TypeCode create_array_tc (int arg0, org.omg.CORBA.TypeCode arg1)  {
		return null;
	}
	
	public org.omg.CORBA.ContextList create_context_list ()  {
		return null;
	}
	
	public org.omg.CORBA.TypeCode create_enum_tc (java.lang.String arg0, java.lang.String arg1, java.lang.String[] arg2)  {
		return null;
	}
	
	public org.omg.CORBA.Environment create_environment ()  {
		return null;
	}
	
	public org.omg.CORBA.ExceptionList create_exception_list ()  {
		return null;
	}
	
	public org.omg.CORBA.TypeCode create_exception_tc (java.lang.String arg0, java.lang.String arg1, org.omg.CORBA.StructMember[] arg2)  {
		return null;
	}
	
	public org.omg.CORBA.TypeCode create_interface_tc (java.lang.String arg0, java.lang.String arg1)  {
		return null;
	}
	
	public org.omg.CORBA.NVList create_list (int arg0)  {
		return null;
	}
	
	public org.omg.CORBA.NamedValue create_named_value (java.lang.String arg0, org.omg.CORBA.Any arg1, int arg2)  {
		return null;
	}
	
	public org.omg.CORBA.portable.OutputStream create_output_stream ()  {
		return null;
	}
	@Deprecated 
	public org.omg.CORBA.TypeCode create_recursive_sequence_tc (int arg0, int arg1)  {
		return null;
	}
	
	public org.omg.CORBA.TypeCode create_sequence_tc (int arg0, org.omg.CORBA.TypeCode arg1)  {
		return null;
	}
	
	public org.omg.CORBA.TypeCode create_string_tc (int arg0)  {
		return null;
	}
	
	public org.omg.CORBA.TypeCode create_struct_tc (java.lang.String arg0, java.lang.String arg1, org.omg.CORBA.StructMember[] arg2)  {
		return null;
	}
	
	public org.omg.CORBA.TypeCode create_union_tc (java.lang.String arg0, java.lang.String arg1, org.omg.CORBA.TypeCode arg2, org.omg.CORBA.UnionMember[] arg3)  {
		return null;
	}
	
	public org.omg.CORBA.TypeCode create_wstring_tc (int arg0)  {
		return null;
	}
	
	public org.omg.CORBA.Context get_default_context ()  {
		return null;
	}
	
	public org.omg.CORBA.Request get_next_response () throws org.omg.CORBA.WrongTransaction {
		return null;
	}
	
	public org.omg.CORBA.TypeCode get_primitive_tc (org.omg.CORBA.TCKind arg0)  {
		return null;
	}
	
	public java.lang.String[] list_initial_services ()  {
		return null;
	}
	
	public java.lang.String object_to_string (org.omg.CORBA.Object arg0)  {
		return null;
	}
}
