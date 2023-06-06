package sudokuPackage;

public class solver{
	int [][] sol;
	
	public solver(int [][] solution){
		sol = solution;
	}

	public boolean inrow(int number, int r){
		for(int i = 0; i < 9; i++){
			if(sol[r][i] == number)
				return true;	
		}
		return false;
	}

	public boolean incollumn(int number, int c){
		for(int i = 0; i < 9; i++){
			if(sol[i][c] == number)
				return true;	
		}
		return false;
	}

	public boolean inbox(int number, int r, int c){
		int boxi = r - (r % 3);
		int boxj = c - (c % 3);
		for(int i = boxi; i < boxi + 3; i++){
			for(int j = boxj; j < boxj + 3; j++){
				if(sol[i][j] == number)
					return true;	
			}
		}
		return false;
	}

	public boolean isok(int number, int r, int c){
		return !inrow(number, r) && !incollumn(number, c) && !inbox(number, r, c);
	}

	public boolean solve(){
		for(int i = 0; i < 9; i++){
			for(int j = 0; j < 9 ; j++){
				if(sol[i][j] == 0){
					for(int try_num = 1; try_num <= 9 ; try_num++){
						if(isok(try_num, i, j)){
							sol[i][j] = try_num;
							if(solve()){
								return true;
							}
							else{
								sol[i][j] = 0;
							}
						}
					}
					return false;
				}		
			}
		}
		return true;
	}
	
	public void print(){
		for(int i = 0; i < 9; i++){
			for(int j = 0; j < 9; j++){
				System.out.print(sol[i][j]+ " ");
			}	
			System.out.println();
		}	
	}
}
