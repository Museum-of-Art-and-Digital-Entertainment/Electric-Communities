
/*  TokenSpending    v 0.1 
 *
 *  This program shows some basis for Token Spending.
 *
 *  06.30.1997  Copyright Electric Communities
 *  All rights reserved world wide
 *
 *  Masa Maeda
 */

 // Two avatars will perfrom a token transaction through a third
 // party (a bank) to ensure no cheat is done.
 // Tokens of a given type exist only within a hub's boundaries.
 // The bank controls an avatar DB that contains a mapping between
 // avatars and the total number of tokens they own.
 // Avatars have no direct access to their tokens to avoid cheat.
 // When, for example, avatar A requests tokens to avatar B, such
 // request consists on a depsit slip that indicates the amount of
 // tokens to transfer and an object to be used as hash key to the
 // actual record where the tokens will be deposited. This enforces
 // secure transactions.

package ec.pl.examples.TokenSpend;

import ec.e.file.EStdio;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;
import ec.e.lang.EInteger;
import ec.e.lang.ELong;
//import ec.e.run.RtEException;
import java.util.Hashtable;
import java.util.Random;
import java.lang.Integer;

// ----------------------- SpendTokens ---------------------------------------
// ------------------------- go() --------------------------------------------
//public eclass SpendTokens implements Agent  {
public eclass SpendTokens implements ELaunchable  {
    //emethod go (EEnvironment env, AgentInfo info)  {
    emethod go (EEnvironment env)  {
        System.out.println("Start token transfer");

    try {
        EStdio.initialize(env.vat());
    } catch (Exception ex) {
        throw new RuntimeException("can't start: " + ex);
    }

        // create bank
        Bank ecBank = new Bank();

        // create a hub.
        Hub hubbit = new Hub();

        // generate depositee and depositor avatars.
        Avatar depositee = new Avatar();
        Avatar depositor = new Avatar();

        // depositee enters hub
        etry  {
            depositee <- goHub(hubbit);
        } ecatch (RuntimeException failed)  {}

        // depositor enters hub
        etry  {
            depositor <- goHub(hubbit);
        } ecatch (RuntimeException failed)  {}

        // Generate a random token amount to transfer through common object
        // between depositee and bank.
        Integer randomInt = new Integer((new Random()).nextInt());
    //  randomInt = ((randomInt.toString()).substring(0,2)).getInteger();
        randomInt = getInteger((randomInt.toString()).substring(0,2));
        EInteger transferAmount = new EInteger(randomInt.intValue());

        // Request deposit slip to Live Account, informing token amount to be 
        // transfered and expecting the deposit slip number.
        ELong slipNum;
        etry  {
            depositee <- requestDepositSlip(transferAmount, &slipNum);
        } ecatch (RuntimeException failed)  {}
        ewhen slipNum (long mySlipNum)  {
            ELong depositSlipNumber = new ELong(mySlipNum);
            depositor <- requestTokenTransfer(transferAmount, depositSlipNumber);
        }

        System.out.println("End of token transfer");
    }
}


// ---------------------------- Bank -----------------------------------------
// The bank holds the DB and takes care of the token spending transaction.
eclass Bank  {
    // create customer DB
    // this DB can only be accessed from within the bank.
    // It contains the avatarID as key and the tokens owned as data object.
    Hashtable customerDB = new Hashtable();
    
    // generate hash table for deposit registrar DB.
    // This is the table used to keep track of each avatar involved in a 
    // transaction. It contains an avatarKey as key and a LiveAccount
    // object as data object.
    Hashtable depositRegistrarDB = new Hashtable();
    
    // generate hash table for deposit transactions.
    // This is the table used to perform token transactions.
    // The key is a deposit slip number and the data object the actual deposit
    // slip.
    Hashtable depositSlipDB = new Hashtable();
            
    // update account for an avatar in customer DB
    emethod updateCustomerDB(ELong avatarID, EInteger tokens) {
      ewhen avatarID (long myAvatarID) {
        Long myID = new Long(myAvatarID);
          ewhen tokens (int myTokens) {
            Integer tokenBalance = new Integer(myTokens);
                customerDB.put(myID, tokenBalance);
        }
      }
    }

    // avatarEnteredHub.
    // generate a key for depositRegistrarDB.
    // if avatar in customerDB then obtain its balance,
    // else create a new bank account for avatar with zero balance and
    //   create its record on customerDB.
    // generate a liveAccount for avatar, containing the key generated and
    //   update depositRegistrarDB with key and new liveAccount
    // distribute the new liveAccount
    emethod avatarEnteredHub(ELong ID, EResult distLiveAccount) {
        Long avatarID;
        ewhen ID (long myID) {
            avatarID = new Long(myID);
        }
        Long avatarKey = new Long((new Random()).nextLong());
        boolean avatarInCustomerDB = customerDB.containsKey(avatarID);
        Integer tokenBalance;
        if (avatarInCustomerDB)  {
            tokenBalance = customerDB.get(avatarID);
        } else {
            tokenBalance = (new EInteger(0));
            updateCustomerDB(avatarID, tokenBalance);
        }
        LiveAccount newLiveAccount = new LiveAccount(avatarID, avatarKey, 
                                                     tokenBalance);
        depositRegistrarDB.put(avatarKey, newLiveAccount);
        distLiveAccount <- forward(newLiveAccount);
    }
    
    // Generate a deposit slip number.
    // Generate a deposit slip and fill in transfer amount and depositee key,
    // Put new deposit slip in deposit slip DB with deposit slip number as key
    // and deposit slip as data object.
    // distribute deposit slip number to depositee, and distribute
    // acknowledgement received from deposit slip.
    emethod makeDepositSlip(Long depositeeKey, 
                            EInteger transferAmount, 
                                                    EResult distDepositSlipNumber, 
                                                    EResult distTransferDone) {
        Long depositSlipNumber = new Long((new Random()).nextLong());
        DepositSlip depositSlip = new DepositSlip();
        depositSlip <- storeTransferAmount(transferAmount);
        depositSlip <- storeDepositeeKey(depositeeKey);
        depositSlipDB.put(depositSlipNumber, depositSlip);
        distDepositSlipNumber <- forward (depositSlipNumber);
        // the rest yet to be analyzed @@@
        depositSlip <- acknowledge(&transferDone);
        ewhen transferDone (boolean tokenTransferDone) {
            distTransferDone <- forward(new EBoolean(tokenTransferDone));
        }
    }

    // get the deposit slip and update it with depositorKey.
    // get depositeeKey from depositSlip and use it to get depositeeLiveAccount.
    // get also depositorLiveAccount
    // get the token balance for both depositor and depositee
    // if depositorBalance >= transferAmount
    //    do the token transfer at depositRegistrar level.
    //    simulate commit
    // else acknowledge failure!
    emethod makeTransferToSlip(ELong depositorAvatarKey, EInteger tokenAmount, 
                                                         ELong depositSlipNumber) {
        LiveAccount depositeeLiveAcc;
        LiveAccount depositorLiveAcc;

        ewhen depositorAvatarKey (long depositorKey) {}
        ewhen tokenAmount (int transferAmount) {}
        ewhen depositSlipNumber (long slipNumber) {}

        DepositSlip depositSlip;
        depositSlip = DepositSlipDB.get(slipNumber);
        depositSlip <- storeDepositorKey((new ELong(depositorKey)));
        DepositSlipDB.put(slipNumber, depositSlip);
        depositSlip <- getDepositeeKey(&depositeeAvatarkey);
        ewhen depositeeAvatarkey (long depositeeKey) {
            depositeeLiveAcc = depositRegistrarDB.get(depositeeKey);
            EInteger depositeeTokens;
            depositeeLiveAcc <- getTokenBalance(&depositeeTokens);
            ewhen depositeeTokens (int depositeeBalance) {}
        }

        depositorLiveAcc = depositRegistrarDB.get(depositorKey);
        EInteger depositorTokens;
        depositorLiveAcc <- getTokenBalance(&depositorTokens);
        ewhen depositorTokens (int depositorBalance) {
            if (depositorBalance >= transferAmount) {
                depositorBalance = depositorBalance - transferAmount;
                depositeeBalance = depositeeBalance + transferAmount;
                EInteger avatarTokenBalance = new EInteger(depositorBalance);
                depositorLiveAcc <- updateTokenBalance(avatarTokenBalance);
                avatarTokenBalance = new EInteger(depositeeBalance);
                depositeeLiveAcc <- updateTokenBalance(avatarTokenBalance);
                depositRegistrarDB.put((new Long(depositorKey)), depositorLiveAcc);
                depositRegistrarDB.put((new Long(depositeeKey)), depositeeLiveAcc);
                //  simulate commit next. actually this simply puts the new balance in
                // customerDB. the real commit will be different.
                //----
                // get token balance from customerDB
                // compare them with original depositRegistrar figures
                // if ok then commit
                // else reset depositRegistrar DB and acknowledge failure
                //----
                depositorLiveAcc <- getID(&depositorAvatarID);
                ewhen depositorAvatarID (long depositorID) {
                    depositorRealBalance = (customerDB.get(
                                            (new Long(depositorID)))).intValue();
                    int depositorOriginalBalance = depositorBalance + transferAmount;
                    depositeeLiveAcc <- getID(&depositeeAvatarID);
                    ewhen depositeeAvatarID (long depositeeID) {
                        depositeeRealBalance = (customerDB.get(
                                                (new Long(depositeeID)))).intValue();
                        int depositeeOriginalBalance = depositeeBalance - transferAmount;
                        if ((depositorRealBalance == depositorOriginalBalance) &&
                          (depositeeRealBalance == depositeeOriginalBalance)) {
                            EInteger acknowldege = avatarTokenBalance;
                            customerDB.put((new Long(depositeeID)), avatarTokenBalance);
                            avatarTokenBalance = new EInteger(depositorBalance);
                        } else {
                            EInteger acknowledge = new EInteger(0);
                        }
                        myDepositSlip <- sendAcknowledgement(acknowldege);
                    }
                }
            } else{
                EInteger acknowledge = new EInteger(0);
                myDepositSlip <- sendAcknowledgement(acknowledge);
            }
        }
    }
}


// -------------------------- Avatar -----------------------------------------
// Avatar class is utilized once at init time to generate
// the ID for each avatar created.
eclass Avatar {
    ELong ID;
    LiveAccount liveAccount;
    ELong depositSlipNumber;

    // Avatar constructor.
    // generate avatar's ID and owned token amount.
    // update bank's customerDB with info generated.
    public Avatar() {
        ID = new ELong((new Random()).nextLong());
        EInteger tokenAmount = new EInteger((new Random()).nextInt());
        etry  {
            ecBank <- updateCustomerDB(ID, tokenAmount);
        } ecatch (RuntimeException failed)  {}
    }

    // avatar enters hub.
    // get avatar's live account generated by bank.
    emethod goHub(Hub hubbit) {
        etry  {
            hubbit <- enterHub(ID, &avatarLiveAcc);
        } ecatch (RuntimeException failed)  {}
        ewhen avatarLiveAcc (LiveAccount myLiveAccount){
            liveAccount = myLiveAccount;
        }
    }

    // avatar asks bank to produce a deposit slip.
    // gets a deposit slip number and awaits acknowledgement
    // over deposit transaction done.
    emethod requestDepositSlip(EInteger tokenAmount, 
                               EResult depositSlipNum) {
        liveAccount <- getDepositSlip(tokenAmount, &depositSlipNum);
        ewhen depositSlipNum (long mySlipNumber) {
            depositSlipNumber = new ELong(mySlipNumber);
        }
    }

    // depositee sends this to depositor to request transfer of tokens.
    // A future version might include a way for depositor to tell user
    // whom the tokens are being transfered to.
    emethod requestTokenTransfer(EInteger transferAmount, 
                                 ELong slipNumber)  {
        etry  {
            depositSlipNumber = slipNumber;
            liveAccount <- transferToSlip(transferAmount, slipNumber);
        } ecatch (RuntimeException failed)  {}
    }
}


// ------------------------------ Hub ----------------------------------------
// A Hub class
eclass Hub  {
    // when entering a hub, avatar shows its ID
    // and hub shows such ID to bank.
    // bank returns the corresponding LiveAccount, if any.
    // hub returns LiveAccount to avatar.
    emethod enterHub(ELong avatarID, EResult distLiveAcc)  {
        etry  {
            ecBank <- avatarEnteredHub(avatarID, &distLiveAcc);
        } ecatch (RuntimeException failed)  {}
    }
}


// ---------------------------- LiveAccount  ---------------------------------
// intermediary class between bank and avatars.
// common objects are generated here
eclass LiveAccount {
    ELong           myID;
    ELong           myKey;
    EInteger    myTokenBalance;
    EInteger    acknowledge;

    public LiveAccount(Long avatarID, Long avatarKey, Integer tokenBalance) {
        myID = new ELong(avatarID.longValue());
        myKey = new ELong(avatarKey.longValue());
        myTokenBalance = new EInteger(tokenBalance.intValue());
    }

    // depositee informs Live Account the token amount expected from depositor.
    // Ask bank to generate deposit slip and get the deposit slip number to 
    // distribute it to avatar. Also, inform avatar when transfer is done.
    emethod getDepositSlip(EInteger transferAmount, 
                           EResult distSlipNumber) {
        etry  {
            ecBank <- makeDepositSlip(myKey, transferAmount, &distSlipNumber, 
                                      &acknowledge);
        } ecatch (RuntimeException failed)  {}
        ewhen acknowledge (int tokenBalance) {
            if (tokenBalance == 0)  {
                System.out.println("huh?! where's the money??? ... transfer failed!");
            } else {
                System.out.println("Cool! Transfer done!");
                System.out.println("New riches total: " + tokenBalance + " tokens!");
            }
        }
    }

    // ask bank to transfer tokens to deposit slip
    emethod transferToSlip(EInteger transferAmount, ELong slipNumber) {
        ecBank <- makeTransferToSlip(myKey, transferAmount, slipNumber);
    }

    emethod updateTokenBalance(EInteger tokenBalance) {
        myTokenBalance = tokenBalance;
    }

    emethod getID(EResult avatarID) {
        avatarID <- forward(myID);
    }
}


// ---------------------------- DepositSlip  ---------------------------------
// class for deposit slips
// contains transfer amount, transferDone flag, and to perform the commit
// the keys for both deposite and depositor.
eclass DepositSlip  {
    EInteger transferAmount;
    EInteger acknowledgement;
    ELong depositeeKey;
    ELong depositorKey;

    // store transfer amount
    emethod storeTransferAmount(EInteger tokenAmount)  {
        transferAmount = tokenAmount;
    }

    // store depositee key
    emethod storeDepositeeKey(ELong avatarKey)  {
        depositeeKey = avatarKey;
    }

    emethod getDepositeeKey(EResult distAvatarkey) {
        distAvatarKey <- forward(depositeeKey);
    }

    // store depositor key
    emethod storeDepositorKey(ELong avatarKey)  {
        depositorKey = avatarKey;
    }

    // makeDepositSlip invokes this method and waits for the distributor
    // to indicate that the token transfer has finished
    emethod acknowldege(EResult distTransferDone) {
        ewhen acknowledgement (boolean transferDone) {
            distTransferDone <- forward (new EBoolean(transferDone));
        }
    }

    // this method is the trigger to distribute end of token transfer flag.
    emethod sendAcknowledement(boolean transferDone) {
        &acknowledgement <- forward(new EBoolean(transferDone));
    }
}


