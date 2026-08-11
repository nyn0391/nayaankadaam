diff --git a/frontend/src/App.tsx b/frontend/src/App.tsx
index 1a2b3c4..5d6e7f8 100644
--- a/frontend/src/App.tsx
+++ b/frontend/src/App.tsx
@@
 import SeatLayoutEditor from './pages/admin/SeatLayoutEditor'
 import BusesPage from './pages/admin/Buses'
 import RoutesPage from './pages/admin/Routes'
 import CreateTripPage from './pages/admin/CreateTrip'
+import SchedulesPage from './pages/admin/Schedules'
@@
           <Route path="/admin/seat-layouts" element={<SeatLayoutEditor />} />
           <Route path="/admin/buses" element={<BusesPage />} />
           <Route path="/admin/routes" element={<RoutesPage />} />
           <Route path="/admin/trips/create" element={<CreateTripPage />} />
+          <Route path="/admin/schedules" element={<SchedulesPage />} />
 
         </Routes>
