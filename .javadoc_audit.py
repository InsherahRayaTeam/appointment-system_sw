import re
from pathlib import Path
roots=[Path('src/main/java/org/example/service'),Path('src/main/java/org/example/notification'),Path('src/main/java/org/example/presentation/gui'),Path('src/main/java/org/example/repository'),Path('src/main/java/org/example/domain')]
java_files=[]
for r in roots:
    if r.exists():
        java_files.extend(sorted(r.rglob('*.java')))
class_pat=re.compile(r'^\s*public\s+(?:abstract\s+|final\s+)?(?:class|interface|enum|record)\s+(\w+)')
method_pat=re.compile(r'^\s*public\s+(?!class\b|interface\b|enum\b|record\b)([^;]*?)\b(\w+)\s*\(([^)]*)\)\s*(?:\{|throws\b)')
cons_pat_tpl=r'^\s*public\s+{name}\s*\(([^)]*)\)\s*(?:\{|throws\b)'
missing=[]
weak=[]
for f in java_files:
    lines=f.read_text(encoding='utf-8').splitlines()
    current_class=None
    cons_pat=None
    for i,line in enumerate(lines, start=1):
        m=class_pat.match(line)
        if m:
            current_class=m.group(1)
            cons_pat=re.compile(cons_pat_tpl.format(name=re.escape(current_class)))
            j=i-1
            while j>=1 and lines[j-1].strip()=="":
                j-=1
            prev=lines[j-1].rstrip() if j>=1 else ''
            if not prev.endswith('*/'):
                missing.append((str(f),i,'class',current_class))
            else:
                k=j-2
                content=''
                while k>=0 and '/**' not in lines[k]:
                    t=lines[k].strip().lstrip('*').strip()
                    if t and not t.startswith('@'):
                        content=t; break
                    k-=1
                low=content.lower()
                if low.startswith('represents '):
                    weak.append((str(f),i,'class',current_class,content))
            continue
        if current_class and cons_pat and cons_pat.match(line):
            if ';' in line:
                continue
            j=i-1
            while j>=1 and lines[j-1].strip()=="":
                j-=1
            prev=lines[j-1].rstrip() if j>=1 else ''
            if not prev.endswith('*/'):
                missing.append((str(f),i,'constructor',current_class))
            continue
        mm=method_pat.match(line)
        if mm:
            name=mm.group(2)
            if name in {'if','for','while','switch','catch','return'}:
                continue
            if ';' in line:
                continue
            j=i-1
            while j>=1 and lines[j-1].strip()=="":
                j-=1
            prev=lines[j-1].rstrip() if j>=1 else ''
            if not prev.endswith('*/'):
                missing.append((str(f),i,'method',name))
            else:
                k=j-2
                content=''
                while k>=0 and '/**' not in lines[k]:
                    t=lines[k].strip().lstrip('*').strip()
                    if t and not t.startswith('@'):
                        content=t; break
                    k-=1
                low=content.lower()
                if low.startswith('returns the ') or low.startswith('checks whether ') or low.startswith('updates the ') or low.startswith('creates a new '):
                    weak.append((str(f),i,'method',name,content))
print(f"Java files scanned: {len(java_files)}")
print(f"Missing Javadocs: {len(missing)}")
print(f"Weak/templated Javadocs flagged: {len(weak)}")
print("\nTop missing entries:")
for row in missing[:120]:
    print('|'.join(map(str,row)))
print("\nTop weak entries:")
for row in weak[:200]:
    print('|'.join(map(str,row)))
