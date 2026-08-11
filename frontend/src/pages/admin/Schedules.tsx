@@
-      if (ruleType === 'WEEKLY') payload.weekdays = weekdays ? JSON.stringify(weekdays.split(',').map(s => s.trim().toUpperCase())) : '[]'
+      if (ruleType === 'WEEKLY') {
+        // normalize user input: accept MON, MONDAY, 1..7 and map to full DayOfWeek names
+        const tokens = weekdays ? weekdays.split(',').map(s => s.trim()).filter(Boolean) : []
+        const mapping: { [k: string]: string } = {
+          MON: 'MONDAY', TUE: 'TUESDAY', WED: 'WEDNESDAY', THU: 'THURSDAY', FRI: 'FRIDAY', SAT: 'SATURDAY', SUN: 'SUNDAY'
+        }
+        const numberMap: { [k: string]: string } = { '1': 'MONDAY', '2': 'TUESDAY', '3': 'WEDNESDAY', '4': 'THURSDAY', '5': 'FRIDAY', '6': 'SATURDAY', '7': 'SUNDAY' }
+        const normalized = tokens.map(t => {
+          const up = t.toUpperCase()
+          if (mapping[up]) return mapping[up]
+          if (numberMap[up]) return numberMap[up]
+          // if already full name
+          return up
+        })
+        payload.weekdays = JSON.stringify(normalized)
+      }
