
Requirements:
 IMDbPY minimum v4.7
 http://imdbpy.sourceforge.net/



Changes on 'get_movie.py'
==============================================================================
To make this magic happen, add these lines just before
 '''print movie.summary().encode(out_encoding, 'replace')'''  (around line 50)

------------------------------------------------------------------------------
imdbURL = i.get_imdbURL(movie)
if imdbURL:
    print 'IMDb URL: %s' % imdbURL

print ""

coverurl = movie.get('cover url')
if coverurl:
    print 'Cover URL: ' + coverurl
else:
    print 'Cover URL:'

coverurl = movie.get('full-size cover url')
if coverurl:
    print 'Full size cover URL: ' + coverurl
else:
    print 'Full size cover URL: '

print ""
------------------------------------------------------------------------------