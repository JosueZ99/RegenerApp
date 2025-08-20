from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.filters import SearchFilter, OrderingFilter
from django_filters.rest_framework import DjangoFilterBackend
from django.shortcuts import get_object_or_404
from decimal import Decimal
import math
from .models import (
    CalculationType, Calculation, PaintCalculation, 
    GypsumCalculation, LEDStripCalculation, CableCalculation
)
from .serializers import (
    CalculationTypeSerializer, CalculationListSerializer,
    CalculationDetailSerializer, PaintCalculationSerializer,
    GypsumCalculationSerializer, LEDStripCalculationSerializer,
    CableCalculationSerializer, PaintCalculationRequestSerializer,
    GypsumCalculationRequestSerializer, LEDStripCalculationRequestSerializer,
    CableCalculationRequestSerializer, AddCalculationToBudgetSerializer,
    CalculationResultSerializer
)
from apps.projects.models import Project
from apps.materials.models import Material

class CalculationTypeViewSet(viewsets.ReadOnlyModelViewSet):
    """
    ViewSet de solo lectura para tipos de calculadoras
    """
    queryset = CalculationType.objects.filter(is_active=True)
    serializer_class = CalculationTypeSerializer
    filter_backends = [SearchFilter, OrderingFilter]
    search_fields = ['name', 'description']
    ordering = ['category', 'name']
    filterset_fields = ['category']

class CalculationViewSet(viewsets.ModelViewSet):
    """
    ViewSet para gestión de cálculos
    """
    queryset = Calculation.objects.all()
    serializer_class = CalculationDetailSerializer
    filter_backends = [SearchFilter, OrderingFilter, DjangoFilterBackend]
    search_fields = ['calculation_type__name', 'material__name']
    ordering_fields = ['created_at', 'estimated_cost']
    ordering = ['-created_at']
    filterset_fields = ['project', 'calculation_type', 'added_to_budget']
    
    def get_serializer_class(self):
        """Retorna serializer según la acción"""
        if self.action == 'list':
            return CalculationListSerializer
        return CalculationDetailSerializer
    
    def get_queryset(self):
        """Personalizar queryset según filtros"""
        queryset = Calculation.objects.all()
        
        # Filtro por proyecto seleccionado
        selected_project_only = self.request.query_params.get('selected_project_only')
        if selected_project_only and selected_project_only.lower() == 'true':
            try:
                selected_project = Project.objects.get(is_selected=True)
                queryset = queryset.filter(project=selected_project)
            except Project.DoesNotExist:
                queryset = queryset.none()
        
        return queryset
    
    @action(detail=False, methods=['post'])
    def calculate_paint(self, request):
        """
        Calculadora de pintura
        POST /api/calculations/calculate_paint/
        """
        serializer = PaintCalculationRequestSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
        data = serializer.validated_data
        
        # Cálculos de pintura
        area = data['area_to_paint']
        coats = data['number_of_coats']
        coverage = data['coverage_per_liter']
        
        # Área total considerando capas
        total_area = area * coats
        
        # Litros necesarios
        liters_needed = total_area / coverage
        
        # Factor de desperdicio (5% por defecto)
        waste_factor = Decimal('0.05')
        liters_with_waste = liters_needed * (1 + waste_factor)
        
        # Convertir a galones (1 galón = 3.785 litros)
        gallons_needed = liters_with_waste / Decimal('3.785')
        
        # Buscar material sugerido
        material = None
        estimated_cost = None
        
        if data.get('material_id'):
            try:
                material = Material.objects.get(id=data['material_id'])
                if material.reference_price:
                    estimated_cost = liters_with_waste * material.reference_price
            except Material.DoesNotExist:
                pass
        
        # Crear registro de cálculo
        calculation_type = CalculationType.objects.get(code='paint')
        
        calculation = Calculation.objects.create(
            project_id=data['project_id'],
            calculation_type=calculation_type,
            material=material,
            input_data=dict(data),
            calculated_quantity=liters_with_waste,
            unit='litros',
            estimated_cost=estimated_cost,
            detailed_results={
                'total_area': float(total_area),
                'base_liters_needed': float(liters_needed),
                'waste_factor_applied': float(waste_factor),
                'gallons_equivalent': float(gallons_needed)
            }
        )
        
        # Crear detalles específicos de pintura
        paint_details = PaintCalculation.objects.create(
            calculation=calculation,
            area_to_paint=area,
            number_of_coats=coats,
            paint_type=data['paint_type'],
            coverage_per_liter=coverage,
            total_liters_needed=liters_with_waste,
            gallons_needed=gallons_needed
        )
        
        # Respuesta unificada
        return Response({
            'calculation_id': calculation.id,
            'calculation_type': 'paint',
            'calculated_quantity': float(liters_with_waste),
            'unit': 'litros',
            'estimated_cost': float(estimated_cost) if estimated_cost else None,
            'detailed_results': calculation.detailed_results,
            'specific_details': {
                'area_to_paint': float(area),
                'number_of_coats': coats,
                'paint_type': data['paint_type'],
                'coverage_per_liter': float(coverage),
                'gallons_needed': float(gallons_needed)
            },
            'material_suggestions': self._get_material_suggestions('construction', 'pintura')
        })
    
    @action(detail=False, methods=['post'])
    def calculate_gypsum(self, request):
        """
        Calculadora de gypsum - CORREGIDA
        POST /api/calculations/calculate_gypsum/
        """
        serializer = GypsumCalculationRequestSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
        data = serializer.validated_data
        
        # ===== CONVERSIÓN A FLOAT PARA EVITAR ERRORES DE TIPO =====
        area = float(data['area_to_cover'])
        thickness = float(data['thickness'])
        
        # Cálculos de gypsum (plancha estándar: 1.22m x 2.44m = 2.98 m²)
        sheet_area = 1.22 * 2.44  # m²
        sheets_base = area / sheet_area
        
        # Factor de desperdicio
        waste_factor = 0.10  # 10% de desperdicio
        sheets_needed = math.ceil(sheets_base * (1 + waste_factor))
        
        # Metros lineales de perfil (estimación: perímetro + refuerzos internos)
        perimeter_estimate = 2 * math.sqrt(area)  # Estimación aproximada del perímetro
        internal_reinforcement = area * 0.5  # 0.5m de perfil por m² de área
        linear_meters_profile = perimeter_estimate + internal_reinforcement
        
        # Buscar material sugerido
        material = None
        estimated_cost = None
        
        if data.get('material_id'):
            try:
                material = Material.objects.get(id=data['material_id'])
                if material.reference_price:
                    estimated_cost = sheets_needed * float(material.reference_price)
            except Material.DoesNotExist:
                pass
        
        # Crear registro de cálculo
        calculation_type = CalculationType.objects.get(code='gypsum')
        
        calculation = Calculation.objects.create(
            project_id=data['project_id'],
            calculation_type=calculation_type,
            material=material,
            input_data=dict(data),
            calculated_quantity=Decimal(str(sheets_needed)),
            unit='planchas',
            estimated_cost=Decimal(str(estimated_cost)) if estimated_cost else None,
            detailed_results={
                'sheet_area': sheet_area,
                'sheets_base': sheets_base,
                'waste_factor_applied': waste_factor,
                'linear_meters_profile': linear_meters_profile
            }
        )
        
        # Crear detalles específicos de gypsum
        gypsum_details = GypsumCalculation.objects.create(
            calculation=calculation,
            area_to_cover=Decimal(str(area)),
            thickness=Decimal(str(thickness)),
            gypsum_type=data['gypsum_type'],
            sheets_needed=sheets_needed,
            linear_meters_profile=Decimal(str(linear_meters_profile))
        )
        
        return Response({
            'calculation_id': calculation.id,
            'calculation_type': 'gypsum',
            'calculated_quantity': sheets_needed,
            'unit': 'planchas',
            'estimated_cost': estimated_cost,
            'detailed_results': calculation.detailed_results,
            'specific_details': {
                'area_to_cover': area,
                'thickness': thickness,
                'gypsum_type': data['gypsum_type'],
                'linear_meters_profile': linear_meters_profile
            },
            'material_suggestions': self._get_material_suggestions('construction', 'gypsum')
        })
    
    @action(detail=False, methods=['post'])
    def calculate_led_strip(self, request):
        """
        Calculadora de cintas LED - COMPLETAMENTE CORREGIDA
        POST /api/calculations/calculate_led_strip/
        """
        serializer = LEDStripCalculationRequestSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
        data = serializer.validated_data
        
        # ===== CONVERSIÓN EXPLÍCITA A FLOAT PARA EVITAR ERRORES DE TIPO =====
        length = float(data['total_length'])
        power_per_meter = float(data['power_per_meter'])
        meters_per_roll = float(data['meters_per_roll'])
        
        print(f"DEBUG LED - length: {length} (type: {type(length)})")
        print(f"DEBUG LED - power_per_meter: {power_per_meter} (type: {type(power_per_meter)})")
        print(f"DEBUG LED - meters_per_roll: {meters_per_roll} (type: {type(meters_per_roll)})")
        
        # Cálculos básicos
        total_power = length * power_per_meter
        
        # Rollos necesarios
        rolls_needed = math.ceil(length / meters_per_roll)
        
        # Drivers necesarios (asumiendo 60W por driver)
        watts_per_driver = 60.0
        drivers_needed = math.ceil(total_power / watts_per_driver)
        
        # Factor de seguridad para drivers (no cargar al 100%)
        safety_factor = 0.8
        drivers_with_safety = math.ceil(total_power / (watts_per_driver * safety_factor))
        
        print(f"DEBUG LED - Cálculos: total_power={total_power}, rolls_needed={rolls_needed}, drivers_with_safety={drivers_with_safety}")
        
        # Buscar material sugerido
        material = None
        estimated_cost = None
        
        if data.get('material_id'):
            try:
                material = Material.objects.get(id=data['material_id'])
                if material.reference_price:
                    estimated_cost = rolls_needed * float(material.reference_price)
            except Material.DoesNotExist:
                pass
        
        # Crear registro de cálculo
        calculation_type = CalculationType.objects.get(code='led_strip')
        
        calculation = Calculation.objects.create(
            project_id=data['project_id'],
            calculation_type=calculation_type,
            material=material,
            input_data=dict(data),
            calculated_quantity=Decimal(str(length)),  # Convertir a Decimal para BD
            unit='metros',
            estimated_cost=Decimal(str(estimated_cost)) if estimated_cost else None,
            detailed_results={
                'total_power': total_power,
                'rolls_needed': rolls_needed,
                'drivers_basic': drivers_needed,
                'drivers_with_safety': drivers_with_safety,
                'safety_factor_applied': safety_factor
            }
        )
        
        # Crear detalles específicos de LED
        led_details = LEDStripCalculation.objects.create(
            calculation=calculation,
            total_length=Decimal(str(length)),
            power_per_meter=Decimal(str(power_per_meter)),
            voltage=data['voltage'],
            strip_type=data['strip_type'],
            total_power=Decimal(str(total_power)),
            drivers_needed=drivers_with_safety,
            meters_per_roll=Decimal(str(meters_per_roll)),
            rolls_needed=rolls_needed
        )
        
        print(f"DEBUG LED - Calculation created with ID: {calculation.id}")
        
        return Response({
            'calculation_id': calculation.id,
            'calculation_type': 'led_strip',
            'calculated_quantity': length,
            'unit': 'metros',
            'estimated_cost': estimated_cost,
            'detailed_results': calculation.detailed_results,
            'specific_details': {
                'total_length': length,
                'power_per_meter': power_per_meter,
                'voltage': data['voltage'],
                'strip_type': data['strip_type'],
                'total_power': total_power,
                'drivers_needed': drivers_with_safety,
                'rolls_needed': rolls_needed,
                'meters_per_roll': meters_per_roll
            },
            'material_suggestions': self._get_material_suggestions('lighting', 'led')
        })
    
    @action(detail=False, methods=['post'])
    def calculate_cable(self, request):
        """
        Calculadora de cables - CORREGIDA
        POST /api/calculations/calculate_cable/
        """
        serializer = CableCalculationRequestSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
        data = serializer.validated_data
        
        # ===== CONVERSIÓN A FLOAT PARA EVITAR ERRORES DE TIPO =====
        length = float(data['total_length'])
        meters_per_roll = float(data['meters_per_roll'])
        
        # Factor de seguridad para cables (10% extra)
        safety_factor = 0.10
        length_with_safety = length * (1 + safety_factor)
        
        # Rollos necesarios
        rolls_needed = math.ceil(length_with_safety / meters_per_roll)
        
        # Buscar material sugerido
        material = None
        estimated_cost = None
        
        if data.get('material_id'):
            try:
                material = Material.objects.get(id=data['material_id'])
                if material.reference_price:
                    estimated_cost = rolls_needed * float(material.reference_price)
            except Material.DoesNotExist:
                pass
        
        # Crear registro de cálculo
        calculation_type = CalculationType.objects.get(code='cable')
        
        calculation = Calculation.objects.create(
            project_id=data['project_id'],
            calculation_type=calculation_type,
            material=material,
            input_data=dict(data),
            calculated_quantity=Decimal(str(length_with_safety)),
            unit='metros',
            estimated_cost=Decimal(str(estimated_cost)) if estimated_cost else None,
            detailed_results={
                'base_length': length,
                'safety_factor_applied': safety_factor,
                'rolls_needed': rolls_needed,
                'total_meters_purchased': rolls_needed * meters_per_roll
            }
        )
        
        # Crear detalles específicos de cable
        cable_details = CableCalculation.objects.create(
            calculation=calculation,
            total_length=Decimal(str(length_with_safety)),
            wire_gauge=data['wire_gauge'],
            cable_type=data['cable_type'],
            installation_type=data['installation_type'],
            rolls_needed=rolls_needed,
            meters_per_roll=Decimal(str(meters_per_roll))
        )
        
        return Response({
            'calculation_id': calculation.id,
            'calculation_type': 'cable',
            'calculated_quantity': length_with_safety,
            'unit': 'metros',
            'estimated_cost': estimated_cost,
            'detailed_results': calculation.detailed_results,
            'specific_details': {
                'total_length': length_with_safety,
                'wire_gauge': data['wire_gauge'],
                'cable_type': data['cable_type'],
                'installation_type': data['installation_type'],
                'rolls_needed': rolls_needed,
                'meters_per_roll': meters_per_roll
            },
            'material_suggestions': self._get_material_suggestions('electrical', 'cable')
        })

    @action(detail=False, methods=['post'])
    def calculate_empaste(self, request):
        """
        Calculadora de empaste - CORREGIDA
        POST /api/calculations/calculate_empaste/
        """
        serializer = EmpasteCalculationRequestSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
        data = serializer.validated_data
        
        # ===== CONVERSIÓN A FLOAT PARA EVITAR ERRORES DE TIPO =====
        area = float(data['area_to_cover'])
        layers = int(data['number_of_layers'])
        
        # Cálculos de empaste (rendimiento típico: 1kg por 2m² por capa)
        kg_per_sqm_per_layer = 0.5  # 0.5 kg por m² por capa
        total_kg_needed = area * layers * kg_per_sqm_per_layer
        
        # Factor de desperdicio
        waste_factor = 0.05  # 5% de desperdicio para empaste
        total_kg_with_waste = total_kg_needed * (1 + waste_factor)
        
        # Sacos necesarios (asumiendo sacos de 25kg)
        kg_per_sack = 25.0
        sacks_needed = math.ceil(total_kg_with_waste / kg_per_sack)
        
        # Buscar material sugerido
        material = None
        estimated_cost = None
        
        if data.get('material_id'):
            try:
                material = Material.objects.get(id=data['material_id'])
                if material.reference_price:
                    estimated_cost = sacks_needed * float(material.reference_price)
            except Material.DoesNotExist:
                pass
        
        # Crear registro de cálculo
        calculation_type = CalculationType.objects.get(code='empaste')
        
        calculation = Calculation.objects.create(
            project_id=data['project_id'],
            calculation_type=calculation_type,
            material=material,
            input_data=dict(data),
            calculated_quantity=Decimal(str(sacks_needed)),
            unit='sacos',
            estimated_cost=Decimal(str(estimated_cost)) if estimated_cost else None,
            detailed_results={
                'total_kg_needed': total_kg_needed,
                'waste_factor_applied': waste_factor,
                'total_kg_with_waste': total_kg_with_waste,
                'kg_per_sack': kg_per_sack
            }
        )
        
        # Crear detalles específicos de empaste
        empaste_details = EmpasteCalculation.objects.create(
            calculation=calculation,
            area_to_cover=Decimal(str(area)),
            empaste_type=data['empaste_type'],
            number_of_layers=layers,
            sacks_needed=sacks_needed,
            total_kg=Decimal(str(total_kg_with_waste))
        )
        
        return Response({
            'calculation_id': calculation.id,
            'calculation_type': 'empaste',
            'calculated_quantity': sacks_needed,
            'unit': 'sacos',
            'estimated_cost': estimated_cost,
            'detailed_results': calculation.detailed_results,
            'specific_details': {
                'area_to_cover': area,
                'empaste_type': data['empaste_type'],
                'number_of_layers': layers,
                'total_kg': total_kg_with_waste
            },
            'material_suggestions': self._get_material_suggestions('construction', 'empaste')
        })

    @action(detail=False, methods=['post'])
    def calculate_profiles(self, request):
        """
        Calculadora de perfiles de aluminio
        POST /api/calculations/calculate_profiles/
        """
        data = request.data
        total_length = Decimal(str(data['total_length']))
        profile_type = data['profile_type']
        profile_size = data['profile_size']
        finish_type = data['finish_type']
        accessories_needed = data.get('accessories_needed', 'Básicos')
        
        # Longitud estándar de perfil (2 metros)
        standard_length = Decimal('2.0')
        
        # Factor de desperdicio del 10%
        waste_factor = Decimal('0.10')
        length_with_waste = total_length * (1 + waste_factor)
        
        # Perfiles necesarios
        profiles_needed = math.ceil(length_with_waste / standard_length)
        
        # Accesorios según el tipo
        accessories_count = profiles_needed if accessories_needed == 'Completos' else math.ceil(profiles_needed / 2)
        
        # Buscar material sugerido
        material = None
        estimated_cost = None
        
        if data.get('material_id'):
            try:
                material = Material.objects.get(id=data['material_id'])
                if material.reference_price:
                    estimated_cost = profiles_needed * material.reference_price
            except Material.DoesNotExist:
                pass
        
        # Crear registro de cálculo
        calculation_type = CalculationType.objects.get(code='profiles')
        
        calculation = Calculation.objects.create(
            project_id=data['project_id'],
            calculation_type=calculation_type,
            material=material,
            input_data=dict(data),
            calculated_quantity=length_with_waste,
            unit='metros',
            estimated_cost=estimated_cost,
            detailed_results={
                'base_length': float(total_length),
                'waste_factor_applied': float(waste_factor),
                'profiles_needed': profiles_needed,
                'standard_length': float(standard_length),
                'accessories_count': accessories_count
            }
        )
        
        return Response({
            'calculation_id': calculation.id,
            'calculation_type': 'profiles',
            'calculated_quantity': float(length_with_waste),
            'unit': 'metros',
            'estimated_cost': float(estimated_cost) if estimated_cost else None,
            'detailed_results': calculation.detailed_results,
            'specific_details': {
                'total_length': float(length_with_waste),
                'profile_type': profile_type,
                'profile_size': profile_size,
                'finish_type': finish_type,
                'profiles_needed': profiles_needed,
                'accessories_count': accessories_count
            },
            'material_suggestions': self._get_material_suggestions('lighting', 'perfil', profile_size)
        })
    
    @action(detail=True, methods=['post'])
    def add_to_budget(self, request, pk=None):
        """
        Agregar cálculo al presupuesto inicial
        POST /api/calculations/calculations/{id}/add_to_budget/
        """
        try:
            calculation = self.get_object()
        except Calculation.DoesNotExist:
            return Response(
                {'error': 'El cálculo no existe'},
                status=status.HTTP_404_NOT_FOUND
            )
        
        # Validar que el cálculo no haya sido agregado previamente
        if calculation.added_to_budget:
            return Response(
                {'error': 'Este cálculo ya fue agregado al presupuesto'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        # Validar datos del request
        serializer = AddCalculationToBudgetSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
        data = serializer.validated_data
        
        # Crear item en presupuesto
        from apps.budgets.models import BudgetItem
        from apps.budgets.serializers import BudgetItemCreateUpdateSerializer
        
        unit_price = data.get('unit_price_override')
        if not unit_price and calculation.material and calculation.material.reference_price:
            unit_price = calculation.material.reference_price
        elif not unit_price:
            unit_price = calculation.estimated_cost / calculation.calculated_quantity if calculation.estimated_cost else 0
        
        budget_data = {
            'project': calculation.project.id,
            'description': f"{calculation.calculation_type.name} - {calculation.calculated_quantity} {calculation.unit}",
            'category': 'construction' if calculation.calculation_type.category == 'construction' else 'lighting',
            'quantity': calculation.calculated_quantity,
            'unit': calculation.unit,
            'unit_price': unit_price,
            'material': calculation.material.id if calculation.material else None,
            'supplier': data.get('supplier_id'),
            'spaces': data.get('spaces', ''),
            'notes': data.get('notes', f"Generado desde calculadora: {calculation.calculation_type.name}"),
            'created_from_calculation': True
        }
        
        budget_serializer = BudgetItemCreateUpdateSerializer(data=budget_data)
        if budget_serializer.is_valid():
            budget_item = budget_serializer.save()
            
            # Marcar cálculo como agregado
            calculation.added_to_budget = True
            calculation.budget_item_created = budget_item
            calculation.save()
            
            return Response({
                'message': 'Cálculo agregado al presupuesto exitosamente',
                'budget_item_id': budget_item.id,
                'calculation': CalculationDetailSerializer(calculation).data
            })
        
        return Response(budget_serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    def _get_material_suggestions(self, category_type, material_type_filter=None, size_filter=None):
        """
        Obtener sugerencias de materiales para el tipo de cálculo
        """
        import logging
        logger = logging.getLogger(__name__)
        
        try:
            # Filtro base por categoría y activos
            materials = Material.objects.filter(
                category__category_type=category_type,
                is_active=True
            )
            
            logger.info(f"Materiales encontrados para {category_type}: {materials.count()}")
            
            # Aplicar filtro de tipo de material si se proporciona
            if material_type_filter:
                materials = materials.filter(name__icontains=material_type_filter)
                logger.info(f"Después de filtrar por '{material_type_filter}': {materials.count()}")
            
            # Aplicar filtro de tamaño si se proporciona (NUEVO)
            if size_filter:
                # Buscar materiales que contengan el tamaño en el nombre
                materials_with_size = materials.filter(name__icontains=size_filter)
                logger.info(f"Después de filtrar por tamaño '{size_filter}': {materials_with_size.count()}")
                
                # Si encuentra materiales con el tamaño exacto, usar esos
                if materials_with_size.exists():
                    materials = materials_with_size
                    logger.info(f"✅ Usando materiales con tamaño específico: {size_filter}")
                else:
                    logger.warning(f"⚠️ No se encontraron materiales para tamaño '{size_filter}', usando materiales generales")
                    # Mantener materiales generales si no encuentra específicos
            
            # Log de materiales encontrados
            for material in materials[:5]:
                logger.info(f"Material encontrado: {material.name} - {material.code} (${material.reference_price})")
            
            # Serializar los materiales (ordenar por precio si tienen)
            materials = materials.order_by('reference_price')
            
            from apps.materials.serializers import MaterialForCalculatorSerializer
            serialized_data = MaterialForCalculatorSerializer(materials[:5], many=True).data
            
            logger.info(f"Datos serializados: {len(serialized_data)} elementos")
            
            return serialized_data
            
        except Exception as e:
            logger.error(f"Error en _get_material_suggestions: {str(e)}")
            return []