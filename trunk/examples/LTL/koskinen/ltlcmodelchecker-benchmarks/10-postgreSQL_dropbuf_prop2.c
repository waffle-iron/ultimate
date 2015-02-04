//#Safe
// ****************************************************
//
//     Making Prophecies with Decision Predicates
//
//              Byron Cook * Eric Koskinen
//                     July 2010
//
// ****************************************************

// Benchmark: pgdropbuf.c
// Property: istemp => G(A!=1)
// Remarks by DD:
// - The first property from the paper is Gp, the second G(p ==> Fq). 
// - It seems to me, the best variant for G(p ==> Fq) is [](AP(istemp!=1) ==> []AP(A!=1)), as this 
//   is close to the comment in the file 
// - Eric said   AG(  AF(R==1) \/ (A!=1) )

//@ ltl invariant positive:  [](AP(A==1) ==> <>AP(R==1));

extern void __VERIFIER_error() __attribute__ ((__noreturn__));
extern void __VERIFIER_assume() __attribute__ ((__noreturn__));
extern int __VERIFIER_nondet_int() __attribute__ ((__noreturn__));

#include <stdio.h>
#define AF_INET 1
#define AF_INET6 2
#define AF_UNIX 3
#define __builtin___snprintf_chk(a,b,c,d,e,f) {}
#define __builtin___object_size(a,b) __VERIFIER_nondet_int()
#define BM_DIRTY 1
#define BM_JUST_DIRTIED 2
#define BM_IO_IN_PROGRESS 3

int family;
char *hostName;
unsigned short portNumber;
char *unixSocketName;
int MaxListen;
int fd, err;
int maxconn;
int one;
int ret;
char *service;
int hint;
int listen_index;
int added;
int addr_ai_family;
int addr;
int MAXADDR;
int ListenSocket_OF_listen_index;
int ret;
char *sock_path;
int addrs;
int rnode;
int istemp;
int firstDelBlock;
int A; int R;
char *bufHdr;
int bufHdr_tag_blockNum;
int bufHdr_tag_blockNum;
int bufHdr_tag_rnode;
int bufHdr_tag_rnode_spcNode;
int bufHdr_tag_rnode_dbNode;
int bufHdr_tag_rnode_relNode;
int bufHdr_flags;
int bufHdr_cntxDirty;
int bufHdr_tag_rnode_relNode;
int LocalRefCount_i;
int LocalBufferDescriptors_i;
int NLocBuffer;
int i;
int NBuffers;
int bufHdr_refcount;

void StrategyInvalidateBuffer(int bufHdr) {}
void WaitIO(int a) {}
int RelFileNodeEquals(int a, int b) 
{ 
	return __VERIFIER_nondet_int(); 
}


istemp = __VERIFIER_nondet_int();
A = 0;
R = 0;
NLocBuffer = __VERIFIER_nondet_int();
NBuffers = __VERIFIER_nondet_int();

void main() {
	//DD: If NBuffers is not larger than 1, the property is trivially not satisfied. So I added the following line:
	__VERIFIER_assume(NBuffers>1);
	if (istemp==1)
	{
		for (i = 0; i < NLocBuffer; i++)
		{
			bufHdr = &LocalBufferDescriptors_i;
			if (RelFileNodeEquals(bufHdr_tag_rnode, rnode) && bufHdr_tag_blockNum >= firstDelBlock)
			{
				if (LocalRefCount_i != 0) ;
				
				bufHdr_flags &= ~(BM_DIRTY | BM_JUST_DIRTIED);
				bufHdr_cntxDirty = 0;
				bufHdr_tag_rnode_relNode = 1; // InvalidOid;
			}
		}
		goto my_exit;
	}

	A = 1; A = 0; // LWLockAcquire(BufMgrLock, LW_EXCLUSIVE);

	for (i = 1; i <= NBuffers; i++)
	{
		bufHdr = __VERIFIER_nondet_int(); // &BufferDescriptors[i - 1];
recheck:
		if (RelFileNodeEquals(bufHdr_tag_rnode, rnode) && bufHdr_tag_blockNum >= firstDelBlock)
		{
			if (bufHdr_flags & BM_IO_IN_PROGRESS)
			{
				WaitIO(bufHdr);
				goto recheck;
			}

			if (bufHdr_refcount != 0);


			bufHdr_flags &= ~(BM_DIRTY | BM_JUST_DIRTIED);
			bufHdr_cntxDirty = 0;

			StrategyInvalidateBuffer(bufHdr);
		}
	}

	R = 1; R = 0; //LWLockRelease(BufMgrLock);
my_exit:
	while(1) { int yyy;yyy=yyy;}
}
