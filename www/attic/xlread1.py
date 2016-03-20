from pyExcelerator import *

def xlread(arg):
        matrix=[[]]

        for sheet_name, values in parse_xls(arg, 'cp1251'):
            for row_idx, col_idx in sorted(values.keys()):
                v = values[(row_idx, col_idx)]

                if isinstance(v, unicode):
                    v = v.encode('cp866', 'backslashreplace')
                else:
                    v = str(v)

                last_row, last_col = len(matrix), len(matrix[-1])

                print last_row, last_col

                while last_row < row_idx:
                    matrix.extend([[]])
                    last_row = len(matrix)

                while last_col < col_idx:
                    matrix[-1].extend([''])
                    last_col = len(matrix[-1])

                matrix[-1].extend([v])

        return matrix
