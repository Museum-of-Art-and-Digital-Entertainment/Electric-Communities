package ec.e.rep;

import java.io.*;       // For FileNotFoundException
import ec.e.db.*;
import ec.regexp.RegularExpression;
// import ec.e.net.RtEARL;   XXX Add back in after this compiles

/** Issues: <p>
<ul>
<li>

What do we do when we were called with a useful parameters argument?
We used the parameters to decode the object in question. Can we still
cache the object? Probably not.
</ul>

*/

public class RepositoryAlias {

  // class constants

  // Caching policy indicators
  
  public static final int RA_RETRIEVE = 0;
  public static final int RA_CACHE = 1;
  public static final int RE_REPLACE = 2;

  // Instance variables

  private String url;
  private int cachingPolicy;
  Object cachedObject = null;

  /** Constructor.

@param <b>url</b> - URL that the alias points to. Possibilities include:
<dl>
<dt>self:#key
  <dd>a record in the current repository under the new key
<dt>repository://path#key
  <dd>a record in another repository
<dt>file://path
  <dd>the entire contents of a file, possibly after having determined the type of data in the file using
  hints like file extension etc.
</dl>

@param <b>cachingPolicy</b> An int, one of the values

<dl>

<dt>ec.e.rep.RepositoryAlias.RA_RETRIEVE

<dd>The data is retrieved fresh every time it is requested.

<dt>ec.e.rep.RepositoryAlias.RA_CACHE

<dd>The AliasObject caches the result after having retrieved it and returns the cached data on subsequent accesses. 

<dt>ec.e.rep.RepositoryAlias.RA_REPLACE

<dd>the AliasObject saves the result after having retrieved it in
place of itself in the current writable repository. This is stronger
than caching since the value will replace (or at least effectively
shadow) the RepositoryAlias object itself so that the original
external object will never again be accessed

</dl>

  */

public RepositoryAlias(String url, int cachingPolicy) {
    this.url = url;
    this.cachingPolicy = cachingPolicy;
  }
  
  /** Access method for caching policy indicator */

  protected int getCachingPolicy(String key,RtDecodingManager decodingManager) { // Allow for more complex future policies
    return cachingPolicy;
  }

  /**  Returns the object that the alias points to; returns null if anything went wrong.
    You do not need to call this in normal use - it's called by the Repository itself. */

  protected Object possibleValue(Repository repository, Repository frontRepository, RtDecodingParameters parameters) {
    Object result = null;
    try {
      result = value(repository,frontRepository,parameters);
    } catch (Exception e) {
      return null;      // for now
    }
    return result;
  }

  static RegularExpression reSelfURL = new RegularExpression("^self:\\(.*\\)");
  static RegularExpression reFileURL = new RegularExpression("^{file,FILE,File}://\\(.*\\)");
  static RegularExpression reRepositoryURL = new RegularExpression("^{rep,REP,Rep}://\\([^#]*\\)#\\(.*\\)");

  /**  Returns the object that the alias points to; Throws appropriate exceptions if something goes wrong.
    You do not need to call this in normal use - it's called by the Repository itself.

@param repository The calling repository.

@param frontRepository The frontmost (and writeable)repository -
needed if you want to store (and override) the results in a writable
repository. Could be the same one as the first parameter, or null (if
you don't want to store the results)

@param parameters An RtDecodingParameters collection.

*/

  protected Object value(Repository repository, Repository frontRepository, RtDecodingParameters parameters)
       throws FileNotFoundException, RepositoryKeyNotFoundException {
     Object result = null;

     if (cachedObject != null) { // Cached object is only valid if parameters is null
       if ((cachingPolicy == RA_CACHE) && (parameters == null))
         return cachedObject;
     }

// XXX To make this compile in ec3     String urls[] = RtEARL.ParseSearchPath(url);
//      int i = 0;
//      int nrUrls = urls.length;

//      while (i < nrUrls) {
//        if (reSelfURL.Match(urls[i])) {
//          String key = reSelfURL.SubMatch(1);
//          if (repository != null) {
//            try {
//          result = repository.get(key,parameters);
//          // System.out.println("Retrieved alias object for key " + key + " and will return " + result);
//            } catch (Exception e) {
//          result = null;
//            }
//            if (result != null) {

//          // Can't cache if we used parameters to decode the
//          // object since those could change and result in a
//          // different object.

//          if ((cachingPolicy == RA_CACHE) && (parameters == null)) cachedObject = result;
//          return result;
//            }
//          }
//          i++;
//          continue;      // Try next url if we have more than one.
//        } else if (reRepositoryURL.Match(urls[i])) {
//          // String pathname = reRepositoryURL.SubMatch(1); // Path name

//          // We decided to posptone implementing these for a while
//          // until the capability issues have been sorted out.

//          // Note: frontRepository is needed so we know which repository to put RA_REPLACE objects in.

//          throw new RepositoryKeyNotFoundException("Repository alias objects in other repositories are not yet supported:" +
//                               urls[i]);
//        } else if (reFileURL.Match(urls[i])) {
//          throw new RepositoryKeyNotFoundException("Repository alias objects that are files are not yet supported:" + urls[i]);
//        } else throw new RepositoryKeyNotFoundException("Repository alias object has badly formed URL:" + urls[i]);
//      }
     return result;
  }
}
