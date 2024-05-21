package ec.e.run;

einterface EResult
{
    emethod forward(Object value);
    emethod forwardException(Throwable t);
}
