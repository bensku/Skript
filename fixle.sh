#####################
#
# Use this with or without the .gitattributes snippet with this Gist
# create a fixle.sh file, paste this in and run it.
# Why do you want this ? Because Git will see diffs between files shared between Linux and Windows due to differences in line ending handling ( Windows uses CRLF and Unix LF)
# This Gist normalizes handling by forcing everything to use Unix style.
#####################


# Fix Line Endings - Force All Line Endings to LF and Not Windows Default CR or CRLF
# Taken largely from: https://help.github.com/articles/dealing-with-line-endings/
# With the exception that we are forcing LF instead of converting to windows style.


#Set LF as your line ending default.
git config --global core.eol lf

#Set autocrlf to false to stop converting between windows style (CRLF) and Unix style (LF)
git config --global core.autocrlf false


#Save your current files in Git, so that none of your work is lost.
git add . -u
git commit -m "Saving files before refreshing line endings"



#Remove the index and force Git to rescan the working directory.
rm .git/index


#Rewrite the Git index to pick up all the new line endings.
git reset


#Show the rewritten, normalized files.

git status


#Add all your changed files back, and prepare them for a commit. This is your chance to inspect which files, if any, were unchanged.

git add -u
# It is perfectly safe to see a lot of messages here that read
# "warning: CRLF will be replaced by LF in file."


#Rewrite the .gitattributes file.
git add .gitattributes


#Commit the changes to your repository.

git commit -m "Normalize all the line endings"
