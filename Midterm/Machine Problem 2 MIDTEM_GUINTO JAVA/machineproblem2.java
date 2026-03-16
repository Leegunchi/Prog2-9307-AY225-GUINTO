public class machineproblem2 {
def det (a):
    a.append(a[0]); a.append(a[1]);
    x = 0
    for i in range(0, len(a)-2):
    y=1;
    for j in range(0, len(a)-2)
        y *= a[i+j][j]
    x += y

    p = 0
    for i in range(0, len(a)-2):
        y=1;
        z=0;
        for j in range(2, -1, -1):
            y *= a[i+z][j]
            z +=1
        z += 1
        p += y
    return x - p
}