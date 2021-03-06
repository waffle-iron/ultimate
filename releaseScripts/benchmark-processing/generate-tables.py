#!/usr/bin/env python2.7
import argparse
import csv 
import re
import os
import sys
import codecs
import itertools
from test.test_math import acc_check


mLatexSettingsMappings = {
'DerefFreeMemtrack-32bit-Z3-Sp-Integer.epf' :'\\sponly',
'DerefFreeMemtrack-32bit-Z3-IcSp-Integer.epf' :'\\spic',
'DerefFreeMemtrack-32bit-Z3-SpLv-Integer.epf' :'\\splv',
'DerefFreeMemtrack-32bit-SMTInterpol-TreeInterpolation-Integer.epf' :'\\smtinterpolip',
'DerefFreeMemtrack-32bit-Z3-NestedInterpolation-Integer.epf' :'\\zzzip',
'DerefFreeMemtrack-32bit-Princess-TreeInterpolation-Integer.epf' :'\\princessip',
'DerefFreeMemtrack-32bit-Z3-FPandBP-Integer.epf' :'FP+BP',
'DerefFreeMemtrack-32bit-Z3-BP-Integer.epf' :'\\wpiclv',
'DerefFreeMemtrack-32bit-Z3-IcSpLv-Integer.epf' : '\\spiclv',
'Reach-32bit-Z3-BP-Bitvector.epf':'\\wpiclv',
'Reach-32bit-Z3-FPandBP-Bitvector.epf':'FP+BP',
'Reach-32bit-Z3-NestedInterpolation-Bitvector.epf':'\\zzzip',
'Reach-32bit-Z3-FP-Bitvector.epf':'\\spiclv',
'Reach-32bit-Princess-TreeInterpolation.epf':'\\princessip',
'Reach-32bit-Z3-FP.epf':'\\spiclv',
'Reach-32bit-SMTInterpol-TreeInterpolation.epf':'\\smtinterpolip',
'Reach-32bit-Z3-BP.epf':'\\wpiclv',
'Reach-32bit-Z3-NestedInterpolation.epf':'\\zzzip',
'Reach-32bit-Z3-IcSp-Bitvector.epf':'\\spic',
'Reach-32bit-Z3-IcSpLv-Bitvector.epf':'\\spiclv',
'Reach-32bit-Z3-IcWp-Bitvector.epf':'\\wpic',
'Reach-32bit-Z3-IcWpLv-Bitvector.epf':'\\wpiclv',
'Reach-32bit-Z3-NestedInterpolation-Bitvector.epf':'\\zzzip',
'Reach-32bit-Z3-Sp-Bitvector.epf':'\\sponly',
'Reach-32bit-Z3-SpLv-Bitvector.epf':'\\splv',
'Reach-32bit-Z3-Wp-Bitvector.epf':'\\wponly',
'Reach-32bit-Z3-WpLv-Bitvector.epf':'\\wplv',
'DerefFreeMemtrack-32bit-Z3-IcWp-Integer.epf':'\\wpic',
'DerefFreeMemtrack-32bit-Z3-IcWpLv-Integer.epf':'\\wpiclv',
'DerefFreeMemtrack-32bit-Z3-Wp-Integer.epf':'\\wponly',
'DerefFreeMemtrack-32bit-Z3-WpLv-Integer.epf':'\\wplv',
'Reach-32bit-Z3-IcSp.epf':'\\spic',
'Reach-32bit-Z3-IcSpLv.epf':'\\spiclv',
'Reach-32bit-Z3-IcWp.epf':'\\wpic',
'Reach-32bit-Z3-IcWpLv.epf':'\\wpiclv',
'Reach-32bit-Z3-Sp.epf':'\\sponly',
'Reach-32bit-Z3-SpLv.epf':'\\splv',
'Reach-32bit-Z3-Wp.epf':'\\wponly',
'Reach-32bit-Z3-WpLv.epf':'\\wplv',
'DerefFreeMemtrack-32bit-Z3-BP-UC-LV-Integer.epf': '\\wpiclv',
'DerefFreeMemtrack-32bit-Z3-FP-UC-Integer.epf': '\\spic',
'DerefFreeMemtrack-32bit-Z3-FP-Integer.epf': '\\sponly',
'DerefFreeMemtrack-32bit-Z3-BP-LV-Integer.epf': '\\wplv',
'DerefFreeMemtrack-32bit-iZ3-NestedInterpolation-Integer.epf': '\\zzzip',
'DerefFreeMemtrack-32bit-Z3-FP-UC-LV-Integer.epf': '\\spiclv',
'DerefFreeMemtrack-32bit-Z3-BP-UC-Integer.epf': '\\wpic',
'DerefFreeMemtrack-32bit-Z3-FP-LV-Integer.epf': '\\splv',
'Reach-32bit-Z3-BP-UC-Integer.epf': '\\wpic',
'Reach-32bit-Z3-FP-LV-Integer.epf': '\\splv',
'Reach-32bit-Z3-FP-Integer.epf': '\\sponly',
'Reach-32bit-Princess-TreeInterpolation-Integer.epf': '\\princessip',
'Reach-32bit-Z3-FP-UC-Integer.epf': '\\spic',
'Reach-32bit-iZ3-NestedInterpolation-Integer.epf': '\\zzzip',
'Reach-32bit-SMTInterpol-TreeInterpolation-Integer.epf': '\\smtinterpolip',
'Reach-32bit-Z3-BP-LV-Integer.epf': '\\wplv',
'Reach-32bit-Z3-BP-Integer.epf': '\\wponly',
'Reach-32bit-Z3-FP-UC-LV-Integer.epf': '\\spiclv',
'Reach-32bit-Z3-BP-UC-LV-Integer.epf': '\\wpiclv'
}

# Those are the dvips colors of xcolor 
# mLatexColors = ['Apricot', 'Aquamarine', 'Bittersweet', 'Black', 'Blue', 'BlueGreen', 'BlueViolet',
#                 'BrickRed', 'Brown', 'BurntOrange', 'CadetBlue', 'CarnationPink', 'Cerulean', 'CornflowerBlue',
#                 'Cyan', 'Dandelion', 'DarkOrchid', 'Emerald', 'ForestGreen', 'Fuchsia', 'Goldenrod', 'Gray',
#                 'Green', 'GreenYellow', 'JungleGreen', 'Lavender', 'LimeGreen', 'Magenta', 'Mahogany', 'Maroon',
#                 'Melon', 'MidnightBlue', 'Mulberry', 'NavyBlue', 'OliveGreen', 'Orange', 'OrangeRed', 'Orchid',
#                 'Peach', 'Periwinkle', 'PineGreen', 'Plum', 'ProcessBlue', 'Purple', 'RawSienna', 'Red',
#                 'RedOrange', 'RedViolet', 'Rhodamine', 'RoyalBlue', 'RoyalPurple', 'RubineRed', 'Salmon',
#                 'SeaGreen', 'Sepia', 'SkyBlue', 'SpringGreen', 'Tan', 'TealBlue', 'Thistle', 'Turquoise',
#                 'Violet', 'VioletRed', 'White', 'WildStrawberry', 'Yellow', 'YellowGreen', 'YellowOrange' ]

mLatexColors = ['s1', 's2', 's3', 's4', 's5', 's6', 's7', 's8', 's9', 'black', 'OliveGreen']

mLatexPlotMarks = ['star', 'triangle', 'diamond', 'x', '|', '10-pointed-star', 'pentagon', 'o']
mLatexPlotMarkRepeat = 10
mLatexPlotLines = ['solid', 'dotted', 'dashed' ]

mUltimateHeader = ['File',
                   'Settings',
                   'Toolchain',
                   'Result',
                   'Overall time',
                   'Overall iterations',
                   'TraceCheckerBenchmark_InterpolantComputationTime',
                   'TraceCheckerBenchmark_Conjuncts in SSA',
                   'TraceCheckerBenchmark_Conjuncts in UnsatCore']

def toPercent(row, a, b):
    part = row[a]
    total = row[b]
    
    if part != None and total != None and part != 'null' and total != 'null':
        totalF = float(total);
        if totalF == 0:
            return 0.0
        return 100.0 * (float(part) / float(total)) 
    return None

def toInt(row, a):
    value = row[a]
    if value != None and value != 'null':
        return int(value)
    return None 

def timeInNanosToSeconds(row, a):
    value = row[a]
    if value != None and value != 'null':
        return float(value) / 1000000000.0
    return None 

def toFloat(row, a):
    value = row[a]
    if value != None and value != 'null':
        return float(value)
    return None 


mRowFuns = { 'Time' : lambda r : timeInNanosToSeconds(r, 'Overall time'),
            'Iter' : lambda r : toInt(r, 'Overall iterations'),
            'InterpolantTime' : lambda r : timeInNanosToSeconds(r, 'TraceCheckerBenchmark_InterpolantComputationTime'),
            'SizeReduction':lambda r : toPercent(r, 'TraceCheckerBenchmark_Conjuncts in UnsatCore', 'TraceCheckerBenchmark_Conjuncts in SSA'),
            'QuantPreds':lambda r : toPercent(r, 'TraceCheckerBenchmark_QuantifiedInterpolants', 'TraceCheckerBenchmark_ConstructedInterpolants'),
            }

def parseArgs():
    # parse command line arguments
    parser = argparse.ArgumentParser(description='Ultimate Latex table generator')
    parser.add_argument('input', type=str, nargs=1, help='A .csv file generated by an Ultimate test suite')
    parser.add_argument('-o', '--output', dest='output', type=str, nargs='?', help='Path to output directory. If not specified, use current working directory.')
    parser.add_argument('-n', '--table-name', dest='name', help='The name of the table we should produce')
    parser.add_argument('-d', '--with-document', dest='withDoc', action='store_true', help='Should we just print the table or also generate a surrounding document?')

    args = parser.parse_args()
    print 'Arguments:', args
    return args

def getSvcompSubFolder(input):
    return re.search('svcomp/(.*)/', input).group(1)

def getSuffix(prefix, input):
    return re.search('.*' + prefix + '(.*)', input).group(1)

def parseCsvFile(fname):
    csvfile = open(fname, 'rb')
    try:
        dialect = csv.Sniffer().sniff(csvfile.read(2048),delimiters=',')
    except csv.Error:
        print "Could not guess .csv dialect, assuming Ultimate defaults"
        csv.register_dialect('ultimate',delimiter=',')
        dialect = 'ultimate'
    csvfile.seek(0)
    return csv.DictReader(csvfile, dialect=dialect)

def applyOnCsvFile(reader, fun, *args):
    acc = None
    for row in reader:
        acc = fun(row, acc, *args)
    return acc

def reduce(col, fun, init, *args):
    acc = init
    for elem in col:
        acc = fun(elem, acc, *args)
    return acc

def printFields(row, acc):
    for field in mUltimateHeader:
        print row[field],
    print
    return

def getUniqueSet(fieldname, row, acc):
    if acc == None:
        acc = set()
    acc.add(row[fieldname])
    return acc

def getFolders(row, acc):
    if acc == None:
        acc = {}
    for field in mUltimateHeader:
        input = row['File']
        key = getSvcompSubFolder(input)
        if(not key in acc):
            acc[key] = []
        acc[key].append(input)
    return acc

def getResultCountPerSetting(result, row, acc):
    if acc == None:
        acc = {}
    
    setting = row['Settings']
    resultCounter = 0
    if setting in acc:
       resultCounter = acc[setting]
    
    if row['Result'] in result:
        acc[setting] = resultCounter + 1 
        
    return acc

def getResultInputPerSetting(result, row, acc):
    if acc == None:
        acc = {}
    
    setting = row['Settings']
    resultInput = set()
    if not setting in acc:
       acc[setting] = resultInput
    else:
        resultInput = acc[setting]
    
    if row['Result'] in result:
        resultInput.add(row['File']) 
        
    return acc

def getExclusivePerSetting(rows, results):
    matchingInputs = applyOnCsvFile(rows, lambda x, y : getResultInputPerSetting(results, x, y))
    acc = {}
    for key, value in matchingInputs.iteritems():
        exclusive = value
        for okey, ovalue in matchingInputs.iteritems():
            if ovalue == value:
                continue
            exclusive = exclusive.difference(ovalue)
            if len(exclusive) == 0:
                break
        acc[key] = exclusive
    return acc

def getExclusiveCountPerSetting(rows, results):
    return mapValues(lambda x : len(x), getExclusivePerSetting(rows, results))

def getMixedInputs(rows, results):
    matchingInputs = applyOnCsvFile(rows, lambda x, y : getResultInputPerSetting(results, x, y))
    shared = set.intersection(*matchingInputs.values())
    exclusive = getExclusivePerSetting(rows, results).values()  
    pure = shared.union(*exclusive)  
    return set.union(*matchingInputs.values()).difference(pure)

def getResultCountPerPortfolio(rows, portfolio, results):
    successCounts = applyOnCsvFile(rows, lambda x, y : getResultInputPerSetting(results, x, y))
    goodResults = set()
    for key, value in successCounts.iteritems():
        if(key in portfolio):
            goodResults = goodResults.union(value)
        
    return len(goodResults)

def getCrashedInputs(rows, uniqueSettings):
    acc = {}
    max = 0
    for row in rows:
        file = row['File']
        if file in acc:
           settings = acc[file]
        else:
            settings = set()
            acc[file] = settings
        settings.add(row['Settings'])
        if max < len(settings):
            max = len(settings)
    
    acc = {k:v for k, v in acc.iteritems() if len(v) < max}
    acc = mapValues(lambda v : uniqueSettings.difference(v), acc)
    return acc

def addRowsForCrashedInputs(rows, crashedInputs, uniqueToolchain):
    if len(rows) == 0:
        return
    
    protoRow = rows[0]
    newrows = {}
    for key, value in crashedInputs.iteritems():
        for setting in value:
            newrow = newRow(protoRow)
            newrow['File'] = key
            newrow['Settings'] = setting
            newrow['Result'] = 'ERROR'
            newrow['Toolchain'] = uniqueToolchain
            rows = rows + [newrow] 
    
    return rows

def newRow(row):
    newrow = {}
    for key in row.iterkeys():
        newrow[key] = None
    return newrow

def getPlottable(rows, rowFun, settings):
    acc = {}
    for setting in settings:
        list = []
        acc[setting] = list
        for row in rows:
            if not row['Settings'] in setting:
                continue
            value = rowFun(row)
            if not value == None:
                list.append(value)
        list.sort()
    return acc


def mapKeys(fun, dicti):
    return dict(map(lambda (k, v): (fun(k), v), dicti.iteritems()))

def mapValues(fun, dicti):
    return dict(map(lambda (k, v): (k, fun(v)), dicti.iteritems()))

def min(val, acc):
    if val > acc:
        return acc
    else:
        return val

def max(val, acc):
    if val > acc:
        return acc
    else:
        return val

def getLatexPlotStyles():
    plotstylesLines = zip(mLatexColors, mLatexPlotLines)
    plotstylesMarks = zip(mLatexColors[len(mLatexPlotLines):], mLatexPlotMarks)
    acc = []
    for color, linestyle in plotstylesLines:
        acc.append('draw=' + color + ',' + linestyle)
    for color, markstyle in plotstylesMarks:
        acc.append('mark repeat={' + str(mLatexPlotMarkRepeat) + '},draw=' + color + ',solid,mark=' + markstyle)
    for color in mLatexColors[len(plotstylesLines) + len(plotstylesMarks):]:
        acc.append('draw=' + color + ',solid')
    return acc

def writeLatexPlotsPreamble(filename):
    f = codecs.open(filename, 'w', 'utf-8')
    f.write('%%%%%%%%%%% Commands for plots %%%%%%%%%%%\n')
    f.write('% argument #1: any options\n')
    f.write('\\newenvironment{customlegend}[1][]{%\n')
    f.write('    \\begingroup\n')
    f.write('    % inits/clears the lists (which might be populated from previous\n')
    f.write('    % axes):\n')
    f.write('    \\csname pgfplots@init@cleared@structures\\endcsname\n')
    f.write('    \\pgfplotsset{#1}%\n')
    f.write('}{%\n')
    f.write('    % draws the legend:\n')
    f.write('    \\csname pgfplots@createlegend\\endcsname\n')
    f.write('    \\endgroup\n')
    f.write('}%\n')
    f.write('\n')
    f.write('% makes \\addlegendimage available (typically only available within an\n')
    f.write('% axis environment):\n')
    f.write('\\def\\addlegendimage{\\csname pgfplots@addlegendimage\\endcsname}\n')
    f.write('\n')
    f.write('\\pgfplotsset{every axis/.append style={thick}}\n')
    f.write('\n')
    
    f.write('\\definecolor{s1}{RGB}{228,26,28}')
    f.write('\\definecolor{s2}{RGB}{55,126,184}')
    f.write('\\definecolor{s3}{RGB}{77,175,74}')
    f.write('\\definecolor{s4}{RGB}{152,78,163}')
    f.write('\\definecolor{s5}{RGB}{255,127,0}')
    f.write('\\definecolor{s6}{RGB}{255,255,51}')
    f.write('\\definecolor{s7}{RGB}{166,86,40}')
    f.write('\\definecolor{s8}{RGB}{247,129,191}')
    f.write('\\definecolor{s9}{RGB}{153,153,153}')
    
    f.write('\\pgfplotsset{\n')
    f.write('    mark repeat/.style={\n')
    f.write('        scatter,\n')
    f.write('        scatter src=x,\n')
    f.write('        scatter/@pre marker code/.code={\n')
    f.write('            \\pgfmathtruncatemacro\\usemark{\n')
    f.write('                or(mod(\\coordindex,#1)==0, (\\coordindex==(\\numcoords-1))\n')
    f.write('            }\n')
    f.write('            \\ifnum\\usemark=0\n')
    f.write('                \\pgfplotsset{mark=none}\n')
    f.write('            \\fi\n')
    f.write('        },\n')
    f.write('        scatter/@post marker code/.code={}\n')
    f.write('    }\n')
    f.write('}\n')
    
    
    f.write('\\pgfplotsset{cycle list={%\n')
    for style in getLatexPlotStyles():
        f.write('{' + style + '},\n')
    f.write('}}\n')

    f.write('%%%%%%%%%%%%% end commands for plots\n')
    f.close()
    return

 
def writeLatexFigureBegin(f, namesAndStyles):
    legendentriesstr = ''
    for name, (file, style) in namesAndStyles:
        legendentriesstr = legendentriesstr + name + ','
    
    f.write('\\onecolumn\n')
    f.write('\\begin{figure}\n')
    f.write('\\centering\n')
    f.write('    \\begin{tikzpicture}\n')
    f.write('    \\begin{customlegend}[legend columns=' + str(len(namesAndStyles) / 2) + ',legend style={align=left,draw=none,column sep=2ex,thick},\n')
    f.write('                          legend entries={' + legendentriesstr + '}]\n')
    for name, (file, style) in namesAndStyles:
        f.write('        \\addlegendimage{' + style + '}\n')
    f.write('    \\end{customlegend}\n')
    f.write('    \\end{tikzpicture}\n')
    return

def writeLatexFigureEnd(f, caption):
    f.write('\\caption{' + caption + '}\n')
    f.write('\\end{figure}\n')
    f.write('\\twocolumn\n')
    return

def writeLatexFigure(f, xlabel, ylabel, files, namesAndStylesDict, caption):
    f.write('\\begin{tikzpicture}\n')
    f.write('\\begin{semilogyaxis}[%\n')
    f.write('log ticks with fixed point,%\n')
    f.write('xmin=0, ymin=0,%\n')
    f.write('xlabel={' + xlabel + '},%\n')
    f.write('ylabel={' + ylabel + '},grid=major,%\n')
    f.write('legend style={at={(0.025,0.975)},anchor=north west,legend cell align=left}%\n')
    f.write(']%\n')
    f.write('\\addlegendimage{empty legend}\\addlegendentry{' + caption + '}\n')
    for file, name in files:
        f.write('\\addplot[' + namesAndStylesDict[name][1] + '] table {plots/' + file + '};\n')
    f.write('\\end{semilogyaxis}\n')
    f.write('\\end{tikzpicture}\n')
    return

def writePlots(successrows, uniqueSettings, outputDir, name):
    plotsfile = os.path.join(outputDir, 'plots.tex')
    if os.path.isfile(plotsfile):
        os.remove(plotsfile)

    plotspreamblefile = os.path.join(outputDir, 'plots-pre.tex')
    writeLatexPlotsPreamble(plotspreamblefile)

    latexFigures = []
    
    for funName, fun in mRowFuns.iteritems():
        plottable = getPlottable(successrows, fun, map(lambda x : (x), uniqueSettings))
        plotfiles = []
        plotnames = []
        for setting, values in plottable.iteritems():
            friendlySetting = os.path.basename(setting)
            filename = funName + '-' + friendlySetting + '.plot'
            f = codecs.open(os.path.join(outputDir, filename), 'w', 'utf-8')
            i = 0
            for val in values:
                f.write(str(i) + ' ' + str(val) + '\n')
                i = i + 1
            f.close()
            if os.stat(f.name).st_size == 0:
                os.remove(f.name)
            else:
                plotfiles.append(filename)
                if friendlySetting in mLatexSettingsMappings:
                    plotnames.append(mLatexSettingsMappings[friendlySetting])
                else:
                    plotnames.append(friendlySetting)
        if name != '':
            funName = name + ' ' + funName
        latexFigures.append((funName, zip(plotfiles, plotnames)))

    namesAndStylesDict = {}
    styles = iter(getLatexPlotStyles())    
    for key, val in latexFigures:
        for file, pname in val:
            if not pname in namesAndStylesDict:
                namesAndStylesDict[pname] = (file, next(styles))
    
    namesAndStyles = sorted(namesAndStylesDict.items())
    
    f = codecs.open(plotsfile, 'a', 'utf-8')
    writeLatexFigureBegin(f, namesAndStyles)
    figCounter = 1
    figPerLine = 3    
    for funName, filesAndNames in latexFigures:
        sortedByName = sorted(filesAndNames, key=lambda x : x[1])
        f.write('\\resizebox*{0.45\\textwidth}{!}{%\n')
        writeLatexFigure(f, 'x', 'y', sortedByName, namesAndStylesDict, funName)
        f.write('}\n')
    writeLatexFigureEnd(f, name)    
    f.close()        
    return

def getArgs():
    args = parseArgs()
    file = args.input[0]
    
    if not os.path.isfile(file):
        print file, 'does not exist'
        sys.exit(1)
        return
    
    output = args.output
    if output == None:
        output = os.getcwd()
    
    name = args.name
    if name == None:
        name = ''
        
    return file, output, name              

def isInt(s):
    try: 
        int(s)
        return True
    except ValueError:
        return False
    except TypeError:
        return False


def getStats(rows, title):
    sortedByIter = sorted(map(lambda row : int(row[title]), filter(lambda row: isInt(row[title]), rows)))
    lenSorted = len(sortedByIter)
    if lenSorted > 0:
        if len(rows) > lenSorted:
            print 'Lost', len(rows) - lenSorted, 'rows for stats because there was no value'
        avg = reduce(sortedByIter, lambda iter, acc : iter + acc, 0) / lenSorted
        min = sortedByIter[0]
        max = sortedByIter[lenSorted - 1]
        if len(rows) % 2 == 0:
            a = lenSorted / 2;
            iterMed = (sortedByIter[lenSorted // 2] + sortedByIter[lenSorted // 2 + 1]) / 2.0
        else:
            iterMed = sortedByIter[lenSorted // 2 + 1]
    else:
        avg = 'N/A'
        min = 'N/A'
        max = 'N/A'
        iterMed = 'N/A'
    return avg, min, max, iterMed

def printStats(rowsEveryoneCouldSolve, setting, column):
    rowsEveryoneCouldSolve = filter(lambda x : x['Settings'] == setting, rowsEveryoneCouldSolve)
    iterAvg, iterMin, iterMax, iterMed = getStats(rowsEveryoneCouldSolve, column)
    print 'ECS', column, 'Avg:', iterAvg
    print 'ECS', column, 'Min:', iterMin
    print 'ECS', column, 'Max:', iterMax
    print 'ECS', column, 'Med:', iterMed

def main():
    file, output, name = getArgs()
    
    rows = list(parseCsvFile(file))
    uniqueSettings = applyOnCsvFile(rows, lambda x, y : getUniqueSet('Settings', x, y))
    uniqueToolchains = applyOnCsvFile(rows, lambda x, y : getUniqueSet('Toolchain', x, y))
    
    if len(uniqueToolchains) > 1:
        print 'We only support 1 toolchain per .csv so far, sorry. You had the following toolchains:'
        print uniqueToolchains
        sys.exit(1)
        return
    
    crashed = getCrashedInputs(rows, uniqueSettings)
    rows = addRowsForCrashedInputs(rows, crashed, next(iter(uniqueToolchains)))
    
    
    uniqueFiles = applyOnCsvFile(rows, lambda x, y : getUniqueSet('File', x, y))
    # for s in uniqueSettings:
    #    print s, len(filter(lambda x : x['Settings'] == s, rows))
    
    successResults = ['SAFE', 'UNSAFE', 'CORRECT', 'INCORRECT']

    renameSettings = lambda x : mLatexSettingsMappings[os.path.basename(x)] if os.path.basename(x) in mLatexSettingsMappings else getSuffix('settings/', x)
    
    solversOnlySettings = filter(lambda x: not re.match('.*Sp.*|.*Wp.*|.*FP.*|.*BP.*', os.path.basename(x)), uniqueSettings)
    championsSettings = solversOnlySettings + filter(lambda x: re.match('.*IcSp.*|.*FP-UC.*', os.path.basename(x)) and not re.match('.*Lv.*', os.path.basename(x),re.IGNORECASE), uniqueSettings)
    looserSettings = filter(lambda x: re.match('.*Sp.*|.*Wp.*|.*FP.*|.*BP.*', os.path.basename(x)) and not re.match('.*UC.*|.*Ic.*|.*LV.*|.*Lv.*', os.path.basename(x)), uniqueSettings)
    
    # # one line of unique settings: total success
    success = applyOnCsvFile(rows, lambda x, y : getResultCountPerSetting(successResults, x, y))
    exclusive = getExclusiveCountPerSetting(rows, successResults)
    allPortfolio = getResultCountPerPortfolio(rows, uniqueSettings, successResults)
    otherPortfolio = getResultCountPerPortfolio(rows, solversOnlySettings, successResults)
    championsPortfolio = getResultCountPerPortfolio(rows, championsSettings, successResults)

    mixed = getMixedInputs(rows, successResults)

    
    remPathD = lambda x : mapKeys(lambda y : renameSettings(y), x)
    remPathS = lambda x : map(lambda y : renameSettings(y), x)

    print 'Settings:         ', remPathS(uniqueSettings)
    print 'Total inputs:     ', len(uniqueFiles)
    # print 'Crashed inputs #: ', len(crashed)
    # print 'Crashed inputs:   ', crashed
    print 'Success:          ', remPathD(success)
    print 'Exclusive success:', remPathD(exclusive)
    print 'All Portfolio:        ', allPortfolio
    print 'Craig Portfolio: ', otherPortfolio
    print 'Craig Portfolio: ', remPathS(solversOnlySettings)
    print 'Craig+IT-SP Portfolio: ', championsPortfolio
    print 'Craig+IT-SP Portfolio: ', remPathS(championsSettings)
    # print 'Mixed:            ', mixed
    print 'Mixed Count:      ', len(mixed)
    
    rowsEveryoneCouldSolve = []
    for r in rows:
        add = True
        for o in rows:
            if o['File'] == r['File'] and not o['Settings'] in looserSettings:
                add = add and o['Result'] in successResults
            if not add:
                break    
        if add:
            rowsEveryoneCouldSolve.append(r)
    
    ecs = [i for i in uniqueSettings if not i in looserSettings]
    print 'Everyone',remPathS(ecs)
    print 'Everyone could solve (ECS):', len(rowsEveryoneCouldSolve) / len(uniqueSettings)
    for s in ecs:
        print '## Setting',  renameSettings(s), '##'
        printStats(rowsEveryoneCouldSolve, s, 'Overall iterations')
        printStats(rowsEveryoneCouldSolve, s, 'Overall time')

    print 
    
    # # gnuplot and stuff 
    successrows = filter(lambda x : x['Result'] in successResults , rows)
    writePlots(successrows, uniqueSettings, output, name)
            
    # applyOnCsvFile(rows, printFields)
    # applyOnCsvFile(rows, lambda x, y : printFields2('haha', x, y))
#     dict = applyOnCsvFile(rows, getFolders)
#     for setting, values in dict.items():
#         print setting
#         for folder in values:
#             print '\t' + folder
    
    return

if __name__ == "__main__":
    main()
