type ref;
type realVar;
type classConst;
// type Field x;
// var $HeapVar : <x>[ref, Field x]x;

const unique $null : ref ;
const unique $intArrNull : [int]int ;
const unique $realArrNull : [int]realVar ;
const unique $refArrNull : [int]ref ;

const unique $arrSizeIdx : int;
var $intArrSize : [int]int;
var $realArrSize : [realVar]int;
var $refArrSize : [ref]int;

var $stringSize : [ref]int;

//built-in axioms 
axiom ($arrSizeIdx == -1);

//note: new version doesn't put helpers in the perlude anymore//Prelude finished 



var int$Random$index0 : int;
var java.lang.String$lp$$rp$$Random$args257 : [int]ref;
var java.lang.Object$Tree$value256 : Field ref;
var Tree$Tree$right255 : Field ref;
var Tree$Tree$left254 : Field ref;


// procedure is generated by joogie.
function {:inline true} $neref(x : ref, y : ref) returns (__ret : int) {
if (x != y) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $realarrtoref($param00 : [int]realVar) returns (__ret : ref);



// procedure is generated by joogie.
function {:inline true} $modreal($param00 : realVar, $param11 : realVar) returns (__ret : realVar);



// procedure is generated by joogie.
function {:inline true} $leref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $modint($param00 : int, $param11 : int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $gtref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $eqrealarray($param00 : [int]realVar, $param11 : [int]realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $addint(x : int, y : int) returns (__ret : int) {
(x + y)
}


// procedure is generated by joogie.
function {:inline true} $subref($param00 : ref, $param11 : ref) returns (__ret : ref);



// procedure is generated by joogie.
function {:inline true} $inttoreal($param00 : int) returns (__ret : realVar);



// procedure is generated by joogie.
function {:inline true} $shrint($param00 : int, $param11 : int) returns (__ret : int);



	 //  @line: 5
// <Tree: void <init>(Tree,Tree)>
procedure void$Tree$$la$init$ra$$2231(__this : ref, $param_0 : ref, $param_1 : ref)
  modifies $HeapVar;
  requires ($neref((__this), ($null))==1);
 {
var r120 : ref;
var r019 : ref;
var r221 : ref;
Block31:
	r019 := __this;
	r120 := $param_0;
	r221 := $param_1;
	 assert ($neref((r019), ($null))==1);
	 //  @line: 6
	 call void$java.lang.Object$$la$init$ra$$28((r019));
	 assert ($neref((r019), ($null))==1);
	 //  @line: 7
	$HeapVar[r019, Tree$Tree$left254] := r120;
	 assert ($neref((r019), ($null))==1);
	 //  @line: 8
	$HeapVar[r019, Tree$Tree$right255] := r221;
	 return;
}


// procedure is generated by joogie.
function {:inline true} $negReal($param00 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $ushrint($param00 : int, $param11 : int) returns (__ret : int);



// <FlattenTree: void <init>()>
procedure void$FlattenTree$$la$init$ra$$2228(__this : ref)  requires ($neref((__this), ($null))==1);
 {
var r01 : ref;
Block16:
	r01 := __this;
	 assert ($neref((r01), ($null))==1);
	 //  @line: 1
	 call void$java.lang.Object$$la$init$ra$$28((r01));
	 return;
}


// procedure is generated by joogie.
function {:inline true} $refarrtoref($param00 : [int]ref) returns (__ret : ref);



// procedure is generated by joogie.
function {:inline true} $divref($param00 : ref, $param11 : ref) returns (__ret : ref);



// procedure is generated by joogie.
function {:inline true} $mulref($param00 : ref, $param11 : ref) returns (__ret : ref);



	 //  @line: 20
// <Tree: Tree createTree()>
procedure Tree$Tree$createTree$2234() returns (__ret : ref)
  modifies $HeapVar;
 {
var i032 : int;
var $r335 : ref;
var $r234 : ref;
var i137 : int;
var r538 : ref;
var r029 : ref;
var $r133 : ref;
var $r436 : ref;
	 //  @line: 21
Block36:
	 //  @line: 21
	 call i137 := int$Random$random$2237();
	 goto Block37;
	 //  @line: 22
Block37:
	 goto Block40, Block38;
	 //  @line: 22
Block40:
	 //  @line: 22
	 assume ($negInt(($neint((i137), (0))))==1);
	 //  @line: 23
	__ret := $null;
	 return;
	 //  @line: 22
Block38:
	 assume ($neint((i137), (0))==1);
	 goto Block39;
	 //  @line: 25
Block39:
	 //  @line: 25
	 call r029 := Tree$Tree$createNode$2233();
	 goto Block41;
	 //  @line: 26
Block41:
	 //  @line: 26
	r538 := r029;
	 goto Block42;
	 //  @line: 28
Block42:
	 goto Block43, Block45;
	 //  @line: 28
Block43:
	 assume ($leint((i137), (0))==1);
	 goto Block44;
	 //  @line: 28
Block45:
	 //  @line: 28
	 assume ($negInt(($leint((i137), (0))))==1);
	 //  @line: 29
	 call i032 := int$Random$random$2237();
	 goto Block46;
	 //  @line: 48
Block44:
	 //  @line: 48
	__ret := r029;
	 return;
	 //  @line: 30
Block46:
	 goto Block49, Block47;
	 //  @line: 30
Block49:
	 //  @line: 30
	 assume ($negInt(($leint((i032), (0))))==1);
	 assert ($neref((r538), ($null))==1);
	 //  @line: 31
	$r335 := $HeapVar[r538, Tree$Tree$left254];
	 goto Block50;
	 //  @line: 30
Block47:
	 assume ($leint((i032), (0))==1);
	 goto Block48;
	 //  @line: 31
Block50:
	 goto Block51, Block53;
	 //  @line: 38
Block48:
	 assert ($neref((r538), ($null))==1);
	 //  @line: 38
	$r133 := $HeapVar[r538, Tree$Tree$right255];
	 goto Block56;
	 //  @line: 31
Block51:
	 assume ($neref(($r335), ($null))==1);
	 goto Block52;
	 //  @line: 31
Block53:
	 //  @line: 31
	 assume ($negInt(($neref(($r335), ($null))))==1);
	 //  @line: 32
	 call $r436 := Tree$Tree$createNode$2233();
	 assert ($neref((r538), ($null))==1);
	 //  @line: 32
	$HeapVar[r538, Tree$Tree$left254] := $r436;
	 //  @line: 33
	r538 := r029;
	 goto Block54;
	 //  @line: 38
Block56:
	 goto Block59, Block57;
	 //  @line: 35
Block52:
	 assert ($neref((r538), ($null))==1);
	 //  @line: 35
	r538 := $HeapVar[r538, Tree$Tree$left254];
	 goto Block55;
	 //  @line: 45
Block54:
	 //  @line: 45
	i137 := $addint((i137), (-1));
	 goto Block60;
	 //  @line: 38
Block59:
	 //  @line: 38
	 assume ($negInt(($neref(($r133), ($null))))==1);
	 //  @line: 39
	 call $r234 := Tree$Tree$createNode$2233();
	 assert ($neref((r538), ($null))==1);
	 //  @line: 39
	$HeapVar[r538, Tree$Tree$right255] := $r234;
	 //  @line: 40
	r538 := r029;
	 goto Block54;
	 //  @line: 38
Block57:
	 assume ($neref(($r133), ($null))==1);
	 goto Block58;
	 //  @line: 35
Block55:
	 goto Block54;
	 //  @line: 46
Block60:
	 goto Block42;
	 //  @line: 42
Block58:
	 assert ($neref((r538), ($null))==1);
	 //  @line: 42
	r538 := $HeapVar[r538, Tree$Tree$right255];
	 goto Block54;
}


// procedure is generated by joogie.
function {:inline true} $neint(x : int, y : int) returns (__ret : int) {
if (x != y) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $ltreal($param00 : realVar, $param11 : realVar) returns (__ret : int);



	 //  @line: 10
// <Tree: void <init>()>
procedure void$Tree$$la$init$ra$$2232(__this : ref)  requires ($neref((__this), ($null))==1);
 {
var r022 : ref;
Block32:
	r022 := __this;
	 assert ($neref((r022), ($null))==1);
	 //  @line: 11
	 call void$java.lang.Object$$la$init$ra$$28((r022));
	 return;
}


// procedure is generated by joogie.
function {:inline true} $reftorefarr($param00 : ref) returns (__ret : [int]ref);



// procedure is generated by joogie.
function {:inline true} $gtint(x : int, y : int) returns (__ret : int) {
if (x > y) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $reftoint($param00 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $addref($param00 : ref, $param11 : ref) returns (__ret : ref);



	 //  @line: 2
// <FlattenTree: void main(java.lang.String[])>
procedure void$FlattenTree$main$2229($param_0 : [int]ref)
  modifies java.lang.String$lp$$rp$$Random$args257, $stringSize;
 {
var r14 : ref;
var r02 : [int]ref;

 //temp local variables 
var $freshlocal0 : ref;

Block17:
	r02 := $param_0;
	 //  @line: 3
	java.lang.String$lp$$rp$$Random$args257 := r02;
	 //  @line: 4
	 call r14 := Tree$Tree$createTree$2234();
	 //  @line: 5
	 call $freshlocal0 := Tree$FlattenTree$flatten$2230((r14));
	 return;
}


// procedure is generated by joogie.
function {:inline true} $xorreal($param00 : realVar, $param11 : realVar) returns (__ret : int);



	 //  @line: 5
// <Random: int random()>
procedure int$Random$random$2237() returns (__ret : int)
  modifies int$Random$index0, $stringSize;
 {
var $r143 : [int]ref;
var $i042 : int;
var $i246 : int;
var $i145 : int;
var r044 : ref;
var $i347 : int;
	 //  @line: 6
Block63:
	 //  @line: 6
	$r143 := java.lang.String$lp$$rp$$Random$args257;
	 //  @line: 6
	$i042 := int$Random$index0;
	 assert ($geint(($i042), (0))==1);
	 assert ($ltint(($i042), ($refArrSize[$r143[$arrSizeIdx]]))==1);
	 //  @line: 6
	r044 := $r143[$i042];
	 //  @line: 7
	$i145 := int$Random$index0;
	 //  @line: 7
	$i246 := $addint(($i145), (1));
	 //  @line: 7
	int$Random$index0 := $i246;
	$i347 := $stringSize[r044];
	 //  @line: 8
	__ret := $i347;
	 return;
}


// procedure is generated by joogie.
function {:inline true} $andref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $cmpreal(x : realVar, y : realVar) returns (__ret : int) {
if ($ltreal((x), (y)) == 1) then 1 else if ($eqreal((x), (y)) == 1) then 0 else -1
}


// procedure is generated by joogie.
function {:inline true} $addreal($param00 : realVar, $param11 : realVar) returns (__ret : realVar);



// procedure is generated by joogie.
function {:inline true} $gtreal($param00 : realVar, $param11 : realVar) returns (__ret : int);



	 //  @line: 8
// <FlattenTree: Tree flatten(Tree)>
procedure Tree$FlattenTree$flatten$2230($param_0 : ref) returns (__ret : ref) {
var $r38 : ref;
var $r816 : ref;
var $r511 : ref;
var r213 : ref;
var r112 : ref;
var r010 : ref;
var $r49 : ref;
var r917 : ref;
var $r715 : ref;
var r1018 : ref;
var $r614 : ref;
Block18:
	r917 := $param_0;
	 //  @line: 9
	r1018 := $null;
	 goto Block19;
	 //  @line: 12
Block19:
	 goto Block20, Block22;
	 //  @line: 12
Block20:
	 assume ($eqref((r917), ($null))==1);
	 goto Block21;
	 //  @line: 12
Block22:
	 //  @line: 12
	 assume ($negInt(($eqref((r917), ($null))))==1);
	 assert ($neref((r917), ($null))==1);
	 //  @line: 14
	$r38 := $HeapVar[r917, Tree$Tree$left254];
	 goto Block23;
	 //  @line: 27
Block21:
	 //  @line: 27
	__ret := r1018;
	 return;
	 //  @line: 14
Block23:
	 goto Block24, Block26;
	 //  @line: 14
Block24:
	 assume ($neref(($r38), ($null))==1);
	 goto Block25;
	 //  @line: 14
Block26:
	 //  @line: 14
	 assume ($negInt(($neref(($r38), ($null))))==1);
	 //  @line: 16
	$r816 := $newvariable((27));
	 assume ($neref(($newvariable((27))), ($null))==1);
	 assert ($neref(($r816), ($null))==1);
	 //  @line: 16
	 call void$Tree$$la$init$ra$$2231(($r816), ($null), (r1018));
	 //  @line: 16
	r1018 := $r816;
	 assert ($neref((r917), ($null))==1);
	 //  @line: 17
	r917 := $HeapVar[r917, Tree$Tree$right255];
	 goto Block19;
	 //  @line: 20
Block25:
	 assert ($neref((r917), ($null))==1);
	 //  @line: 20
	$r49 := $HeapVar[r917, Tree$Tree$left254];
	 goto Block28;
	 //  @line: 20
Block28:
	 assert ($neref(($r49), ($null))==1);
	 //  @line: 20
	r010 := $HeapVar[$r49, Tree$Tree$left254];
	 assert ($neref((r917), ($null))==1);
	 //  @line: 21
	$r511 := $HeapVar[r917, Tree$Tree$left254];
	 assert ($neref(($r511), ($null))==1);
	 //  @line: 21
	r112 := $HeapVar[$r511, Tree$Tree$right255];
	 assert ($neref((r917), ($null))==1);
	 //  @line: 22
	r213 := $HeapVar[r917, Tree$Tree$right255];
	 //  @line: 23
	$r614 := $newvariable((29));
	 assume ($neref(($newvariable((29))), ($null))==1);
	 //  @line: 23
	$r715 := $newvariable((30));
	 assume ($neref(($newvariable((30))), ($null))==1);
	 assert ($neref(($r715), ($null))==1);
	 //  @line: 23
	 call void$Tree$$la$init$ra$$2231(($r715), (r112), (r213));
	 assert ($neref(($r614), ($null))==1);
	 //  @line: 23
	 call void$Tree$$la$init$ra$$2231(($r614), (r010), ($r715));
	 //  @line: 23
	r917 := $r614;
	 goto Block19;
}


// procedure is generated by joogie.
function {:inline true} $eqreal(x : realVar, y : realVar) returns (__ret : int) {
if (x == y) then 1 else 0
}


	 //  @line: 50
// <Tree: void main(java.lang.String[])>
procedure void$Tree$main$2235($param_0 : [int]ref)
  modifies java.lang.String$lp$$rp$$Random$args257, $stringSize;
 {
var r039 : [int]ref;

 //temp local variables 
var $freshlocal0 : ref;

Block61:
	r039 := $param_0;
	 //  @line: 51
	java.lang.String$lp$$rp$$Random$args257 := r039;
	 //  @line: 52
	 call $freshlocal0 := Tree$Tree$createTree$2234();
	 return;
}


// procedure is generated by joogie.
function {:inline true} $ltint(x : int, y : int) returns (__ret : int) {
if (x < y) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $newvariable($param00 : int) returns (__ret : ref);



// procedure is generated by joogie.
function {:inline true} $divint($param00 : int, $param11 : int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $geint(x : int, y : int) returns (__ret : int) {
if (x >= y) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $mulint($param00 : int, $param11 : int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $leint(x : int, y : int) returns (__ret : int) {
if (x <= y) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $shlref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $eqrefarray($param00 : [int]ref, $param11 : [int]ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $reftointarr($param00 : ref) returns (__ret : [int]int);



// procedure is generated by joogie.
function {:inline true} $ltref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $mulreal($param00 : realVar, $param11 : realVar) returns (__ret : realVar);



// <java.lang.Object: void <init>()>
procedure void$java.lang.Object$$la$init$ra$$28(__this : ref);



// procedure is generated by joogie.
function {:inline true} $shrref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $ushrreal($param00 : realVar, $param11 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $shrreal($param00 : realVar, $param11 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $divreal($param00 : realVar, $param11 : realVar) returns (__ret : realVar);



// procedure is generated by joogie.
function {:inline true} $orint($param00 : int, $param11 : int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $reftorealarr($param00 : ref) returns (__ret : [int]realVar);



// procedure is generated by joogie.
function {:inline true} $cmpref(x : ref, y : ref) returns (__ret : int) {
if ($ltref((x), (y)) == 1) then 1 else if ($eqref((x), (y)) == 1) then 0 else -1
}


// procedure is generated by joogie.
function {:inline true} $realtoint($param00 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $geref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $orreal($param00 : realVar, $param11 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $eqint(x : int, y : int) returns (__ret : int) {
if (x == y) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $ushrref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $modref($param00 : ref, $param11 : ref) returns (__ret : ref);



// procedure is generated by joogie.
function {:inline true} $eqintarray($param00 : [int]int, $param11 : [int]int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $negRef($param00 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $lereal($param00 : realVar, $param11 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $nereal(x : realVar, y : realVar) returns (__ret : int) {
if (x != y) then 1 else 0
}


// <java.lang.String: int length()>
procedure int$java.lang.String$length$59(__this : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $instanceof($param00 : ref, $param11 : classConst) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $xorref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $orref($param00 : ref, $param11 : ref) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $intarrtoref($param00 : [int]int) returns (__ret : ref);



// procedure is generated by joogie.
function {:inline true} $subreal($param00 : realVar, $param11 : realVar) returns (__ret : realVar);



// procedure is generated by joogie.
function {:inline true} $shlreal($param00 : realVar, $param11 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $negInt(x : int) returns (__ret : int) {
if (x == 0) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $gereal($param00 : realVar, $param11 : realVar) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $eqref(x : ref, y : ref) returns (__ret : int) {
if (x == y) then 1 else 0
}


// procedure is generated by joogie.
function {:inline true} $cmpint(x : int, y : int) returns (__ret : int) {
if (x < y) then 1 else if (x == y) then 0 else -1
}


// procedure is generated by joogie.
function {:inline true} $andint($param00 : int, $param11 : int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $andreal($param00 : realVar, $param11 : realVar) returns (__ret : int);



	 //  @line: 14
// <Tree: Tree createNode()>
procedure Tree$Tree$createNode$2233() returns (__ret : ref)
  modifies $HeapVar;
 {
var $r225 : ref;
var r024 : ref;
var $r123 : ref;
	 //  @line: 15
Block33:
	 //  @line: 15
	$r123 := $newvariable((34));
	 assume ($neref(($newvariable((34))), ($null))==1);
	 assert ($neref(($r123), ($null))==1);
	 //  @line: 15
	 call void$Tree$$la$init$ra$$2232(($r123));
	 //  @line: 15
	r024 := $r123;
	 //  @line: 16
	$r225 := $newvariable((35));
	 assume ($neref(($newvariable((35))), ($null))==1);
	 assert ($neref(($r225), ($null))==1);
	 //  @line: 16
	 call void$java.lang.Object$$la$init$ra$$28(($r225));
	 assert ($neref((r024), ($null))==1);
	 //  @line: 16
	$HeapVar[r024, java.lang.Object$Tree$value256] := $r225;
	 //  @line: 17
	__ret := r024;
	 return;
}


// procedure is generated by joogie.
function {:inline true} $shlint($param00 : int, $param11 : int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $xorint($param00 : int, $param11 : int) returns (__ret : int);



// procedure is generated by joogie.
function {:inline true} $subint(x : int, y : int) returns (__ret : int) {
(x - y)
}


// <Random: void <init>()>
procedure void$Random$$la$init$ra$$2236(__this : ref)  requires ($neref((__this), ($null))==1);
 {
var r041 : ref;
Block62:
	r041 := __this;
	 assert ($neref((r041), ($null))==1);
	 //  @line: 1
	 call void$java.lang.Object$$la$init$ra$$28((r041));
	 return;
}


	 //  @line: 2
// <Random: void <clinit>()>
procedure void$Random$$la$clinit$ra$$2238()
  modifies int$Random$index0;
 {
	 //  @line: 3
Block64:
	 //  @line: 3
	int$Random$index0 := 0;
	 return;
}

