package ec.e.io;

/**
  EObjects that wish to recieve input from the console should implement this
  einterface. The console thread will send each line using this message.

  @param line A line of console input. Will be null on EOF or error.
*/
public einterface EInputHandler {
    handleInput(String line);
}
