//#Safe
/* 
 * Author: langt@informatik.uni-freiburg.de, heizmann@informatik.uni-freiburg.de
 * Date: 24.08.2015
 */

int main() {
	/* bitwise complement */
	{
		int x = 0;
		int y = ~x;
		//@ assert y == -1;
	}

    /* left shift */
    {
        int x = 2;
        int y = x << 2;
        //@ assert y == 8;
    }

    /* unsigned right shift */
    {
        unsigned int x = 16U;
        unsigned int y = x >> 2U;
        if (y != 4U) {
          //@ assert \false;
        }
    }

    /* signed right shift */
    {
        int x = 16;
        int y = x >> 2;
        //@ assert y == 4;
    }
}