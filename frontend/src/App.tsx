diff --git a/frontend/src/App.tsx b/frontend/src/App.tsx
index 5d6e7f8..8a9b0c1 100644
--- a/frontend/src/App.tsx
+++ b/frontend/src/App.tsx
@@
 import CreateTripPage from './pages/admin/CreateTrip'
 import SchedulesPage from './pages/admin/Schedules'
+import TripSeatMap from './pages/TripSeatMap'
@@
           <Route path="/admin/trips/create" element={<CreateTripPage />} />
           <Route path="/admin/schedules" element={<SchedulesPage />} />
+          <Route path="/trips/:instanceId/seats" element={<TripSeatMap />} />
 
         </Routes>
