#!/bin/bash


TOOLNAME=Automizer
TARGETDIR=Ultimate${TOOLNAME}
TOOLCHAIN=../../trunk/examples/toolchains/AutomizerC.xml
TERMTOOLCHAIN=../../trunk/examples/toolchains/BuchiAutomizerCWithBlockEncoding.xml
SETTINGS=../../trunk/examples/settings/svcomp2016/*${TOOLNAME}*
TERMSETTINGS="$TARGETDIR"/svcomp-Termination-64bit-Automizer.epf

rm -r "$TARGETDIR"
rm Ultimate"$TOOLNAME".zip
mkdir "$TARGETDIR"
cp -a ../../trunk/source/BA_SiteRepository/target/products/CLI-E4/linux/gtk/x86_64/* "$TARGETDIR"/
cp "$TOOLCHAIN" "$TARGETDIR"/"$TOOLNAME".xml
cp "$TERMTOOLCHAIN" "$TARGETDIR"/"$TOOLNAME"Termination.xml
cp LICENSE* "$TARGETDIR"/
cp ${SETTINGS} "$TARGETDIR"/.
cp Ultimate.py "$TARGETDIR"/
cp Ultimate.ini "$TARGETDIR"/
cp README "$TARGETDIR"/
cp z3 "$TARGETDIR"/

# change Z3 memory settings for rcfgbuilder 
#for i in "$TARGETDIR"/*.epf; do awk '/@de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder=0.0.1/ { print; print "/instance/de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder/Command\\ for\\ external\\ solver=z3 SMTLIB2_COMPLIANT\\=true -memory\\:2500 -smt2 -in -t\\:12000"; next }1' $i > $i.tmp && mv $i.tmp $i ; done

# change Z3 memory settings for buchiautomizer
#awk '/@de.uni_freiburg.informatik.ultimate.plugins.generator.buchiautomizer=0.0.1/ { print; print "/instance/de.uni_freiburg.informatik.ultimate.plugins.generator.buchiautomizer/Command\\ for\\ external\\ solver\\ (rank\\ synthesis)=z3 SMTLIB2_COMPLIANT\\=true -memory\\:2500 -smt2 -in -t\\:12000"; next }1' $TERMSETTINGS > $TERMSETTINGS.tmp && mv $TERMSETTINGS.tmp $TERMSETTINGS

# set version 
VERSION=`git rev-parse HEAD | cut -c1-8`
echo $VERSION
sed "s/version =.*/version = \'$VERSION\'/g" "$TARGETDIR"/Ultimate.py > "$TARGETDIR"/Ultimate.py.tmp && mv "$TARGETDIR"/Ultimate.py.tmp "$TARGETDIR"/Ultimate.py && chmod a+x "$TARGETDIR"/Ultimate.py

zip -q Ultimate"$TOOLNAME".zip -r "$TARGETDIR"/*
